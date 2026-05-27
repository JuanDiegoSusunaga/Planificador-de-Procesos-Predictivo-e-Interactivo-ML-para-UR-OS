import pandas as pd
import os
import json
from sklearn.tree import DecisionTreeClassifier
from sklearn.preprocessing import LabelEncoder

base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
data_path = os.path.join(base_dir, 'datasets', 'telemetry_data.csv')
output_dir = os.path.join(base_dir, 'models')
os.makedirs(output_dir, exist_ok=True)


# Cargar y entrenar
df = pd.read_csv(data_path)
df['should_prioritize'] = 0
for intent in df['UserIntent'].unique():
    mask = df['UserIntent'] == intent
    median_time = df.loc[mask, 'TurnaroundTime'].median()
    df.loc[mask & (df['TurnaroundTime'] < median_time), 'should_prioritize'] = 1

le = LabelEncoder()
df['UserIntentCode'] = le.fit_transform(df['UserIntent'])

X = df[['UserIntentCode', 'CpuCycles', 'IoBlocks']]
y = df['should_prioritize']

clf = DecisionTreeClassifier(max_depth=4, min_samples_split=30, random_state=42)
clf.fit(X, y)

# Función para extraer reglas del árbol a JSON
def tree_to_json_rules(tree, feature_names):
    """Convierte el árbol de decisión a reglas en JSON"""
    children_left = tree.tree_.children_left
    children_right = tree.tree_.children_right
    feature = tree.tree_.feature
    threshold = tree.tree_.threshold
    value = tree.tree_.value
    
    rules = []
    
    def extract_rules(node, conditions):
        if children_left[node] != children_right[node]:
            # Nodo no hoja
            feat_name = feature_names[feature[node]]
            thresh = threshold[node]
            
            # Rama izquierda (<=)
            left_conditions = conditions + [f"{feat_name} <= {thresh:.2f}"]
            extract_rules(children_left[node], left_conditions)
            
            # Rama derecha (>)
            right_conditions = conditions + [f"{feat_name} > {thresh:.2f}"]
            extract_rules(children_right[node], right_conditions)
        else:
            # Nodo hoja - decisión
            decision = 1 if value[node][0][1] > value[node][0][0] else 0
            rules.append({
                "conditions": conditions,
                "decision": decision,
                "confidence": float(max(value[node][0]) / sum(value[node][0]))
            })
    
    extract_rules(0, [])
    return rules

# Exportar reglas a JSON
rules = tree_to_json_rules(clf, ['UserIntentCode', 'CpuCycles', 'IoBlocks'])

# Crear JSON completo con metadata
output_data = {
    "model_type": "DecisionTree",
    "version": "1.0",
    "max_depth": int(clf.tree_.max_depth),
    "feature_names": ["UserIntentCode", "CpuCycles", "IoBlocks"],
    "intent_mapping": {
        "dev": 0,
        "office": 1,
        "multimedia": 2
    },
    "description": "Reglas para priorización de procesos según perfil de usuario",
    "rules": rules,
    "usage_example": {
        "input": {
            "UserIntentCode": 1,
            "CpuCycles": 15,
            "IoBlocks": 5
        },
        "how_to_evaluate": "Recorrer las reglas en orden, la primera que cumpla todas las condiciones devuelve su decisión"
    }
}

# Guardar JSON
json_path = os.path.join(output_dir, 'scheduler_rules.json')
with open(json_path, 'w', encoding='utf-8') as f:
    json.dump(output_data, f, indent=2, ensure_ascii=False)


# También guardar una versión simplificada (solo reglas)
simple_rules = []
for rule in rules:
    simple_rules.append({
        "if": " AND ".join(rule["conditions"]),
        "then": rule["decision"]
    })

simple_path = os.path.join(output_dir, 'scheduler_rules_simple.json')
with open(simple_path, 'w', encoding='utf-8') as f:
    json.dump(simple_rules, f, indent=2, ensure_ascii=False)

print(f"   ✓ Versión simplificada: {simple_path}")
