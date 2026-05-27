import pandas as pd
import numpy as np
from sklearn.tree import DecisionTreeClassifier, export_text
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import LabelEncoder
from sklearn.metrics import classification_report
import os

# Configurar rutas
base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
data_path = os.path.join(base_dir, 'datasets', 'telemetry_data.csv')
output_dir = os.path.join(base_dir, 'models')
os.makedirs(output_dir, exist_ok=True)

print("=" * 50)
print("Phase 2: Decision Tree Training for UR-OS")
print("=" * 50)

# Cargar datos
df = pd.read_csv(data_path)
print(f"   Datos cargados: {df.shape[0]} filas, {df.shape[1]} columnas")
print(f"   Columnas: {list(df.columns)}")

# Crear la columna objetivo 'should_prioritize'
df['should_prioritize'] = 0
for intent in df['UserIntent'].unique():
    mask = df['UserIntent'] == intent
    median_time = df.loc[mask, 'TurnaroundTime'].median()
    df.loc[mask & (df['TurnaroundTime'] < median_time), 'should_prioritize'] = 1
    print(f"   {intent}: mediana={median_time:.1f}ms, "
          f"prioritizados={df.loc[mask & (df['TurnaroundTime'] < median_time)].shape[0]} "
          f"/ {df.loc[mask].shape[0]}")


# Codificar UserIntent
le = LabelEncoder()
df['UserIntentCode'] = le.fit_transform(df['UserIntent'])
intent_mapping = dict(zip(le.classes_, le.transform(le.classes_)))
print(f"   Mapeo: {intent_mapping}")

# Preparar features
features = ['UserIntentCode', 'CpuBursts', 'IoBlocks']
X = df[features]
y = df['should_prioritize']

# Entrenar árbol de decisión
X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.2, random_state=42
)

clf = DecisionTreeClassifier(
    max_depth=4,
    min_samples_split=30,
    random_state=42
)
clf.fit(X_train, y_train)

# Evaluar
accuracy = clf.score(X_test, y_test)
print(f"\n5. Evaluación del modelo:")
print(f"   Accuracy: {accuracy:.3f} ({accuracy*100:.1f}%)")
print(f"\n   Classification Report:")
print(classification_report(y_test, clf.predict(X_test)))

# Exportar reglas legibles
tree_rules = export_text(clf, feature_names=features)
with open(os.path.join(output_dir, 'tree_rules.txt'), 'w', encoding='utf-8') as f:
    f.write("=" * 60 + "\n")
    f.write("Decision Tree Rules for UR-OS Scheduler\n")
    f.write("=" * 60 + "\n\n")
    f.write(tree_rules)
