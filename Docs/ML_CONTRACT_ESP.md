# Contrato de Integración ML → Java

Este documento especifica qué debe entregar la Fase 2 (Científica de Datos, Mariana Sandoval) para que el `ML_Scheduler` de Fase 3 lo consuma sin retrabajo.

**Punto único de integración:** `src/ur_os/process/planning/predictive/`

---

## 1. Forma de entrega (elegir UNA)

### Opción A — Clase Java drop-in (recomendada)

Entregar un archivo `TreeEvaluator.java` con esta forma exacta:

```java
package ur_os.process.planning.predictive;

import ur_os.telemetry.ProcessMetrics;
import ur_os.telemetry.UserProfile;

public class TreeEvaluator implements ML_ModelEvaluator {

    @Override
    public boolean shouldPrioritize(ProcessMetrics metrics, UserProfile profile) {
        int cpu = metrics.getCpuBursts();   // ciclos totales de CPU ejecutados
        int io  = metrics.getIoBlocks();

        // ────── Código autogenerado por sklearn.tree.export_text o similar ──────
        if (profile == UserProfile.DEVELOPMENT) {
            if (cpu <= 8) {
                return false;
            } else {
                if (io <= 1) return true;
                else return false;
            }
        }
        // ... resto de perfiles
        return false;
    }
}
```

- **Nombre del archivo:** `TreeEvaluator.java` (o cualquier nombre, siempre que la clase implemente `ML_ModelEvaluator`).
- **Paquete:** `ur_os.process.planning.predictive` (idéntico a la interfaz).
- **Reglas:** una cascada de `if-else` puro. Sin librerías externas, sin reflexión, sin Streams. El código debe ejecutarse en sub-milisegundo (la Fase 3 lo invoca en cada cambio de contexto).

**Activación:** en `ReadyQueue.java` se descomenta:

```java
s = new ML_Scheduler(os, new TreeEvaluator());
```

### Opción B — JSON con reglas

Entregar `ml_training/models/decision_tree.json` con esta estructura:

```json
{
  "version": 1,
  "model_type": "decision_tree",
  "rules": [
    {
      "profile": "DEVELOPMENT",
      "conditions": [
        { "feature": "cpuBursts", "op": "<=", "value": 8, "outcome": "prioritize=false" },
        { "feature": "cpuBursts", "op": ">",  "value": 8, "next": [
            { "feature": "ioBlocks", "op": "<=", "value": 1, "outcome": "prioritize=true" }
        ]}
      ]
    }
  ]
}
```

En este caso, Fase 3 escribirá un `JsonTreeEvaluator` que parsea el archivo al arrancar. **Costo adicional:** ~1 día de trabajo extra para Fase 3. Por eso preferimos Opción A.

---

## 2. Features disponibles

El evaluador recibe `(ProcessMetrics metrics, UserProfile profile)`. Estos son los únicos campos que se pueden leer:

| Campo | Tipo | Semántica | Rango esperado |
|-------|------|-----------|----------------|
| `metrics.getCpuBursts()` | `int` | **Ciclos totales de CPU ejecutados** (nombre legacy, ver §4). | 0–30 |
| `metrics.getIoBlocks()`  | `int` | Veces que el proceso ha sido bloqueado por I/O. | 0–3 |
| `profile` | `UserProfile` | Enum: `DEVELOPMENT`, `OFFICE`, `MULTIMEDIA`, `DEFAULT`. | — |

No hay acceso a otros campos (PID, prioridad, tamaño, etc.) en la interfaz actual. Si necesitas más features para entrenar, comunícame antes — extender `ProcessMetrics` requiere coordinar con Geronimo.

---

## 3. Output

`boolean shouldPrioritize(...)`:
- `true` → el proceso debe ir al frente de la ready queue en el próximo cambio de contexto.
- `false` → tratamiento FCFS normal.

No hay valores intermedios (scoring, probabilidades). Si tu modelo entrega probabilidad, aplica un threshold ≥ 0.5.

---

## 4. Nota crítica sobre `CpuBursts`

Por compatibilidad con la cabecera del CSV de Fase 1, el campo se sigue llamando `CpuBursts` en `ProcessMetrics` y en `telemetry_data.csv`. Sin embargo, **a partir de los commits `7e7afd9` / `36de596` / `d3d1b14`** lo que se exporta es **ciclos totales de CPU ejecutados** (cada tick de instrucción CPU suma 1), no el número de ráfagas distintas.

**Implicación para tu entrenamiento:**
- Si entrenas sobre el CSV actual (anterior al rename), tu modelo verá `CpuBursts=2` para todos los procesos → árbol degenerado.
- **Solución:** pedirle a Geronimo regenerar el dataset corriendo el simulador después del cambio, o trabajar sobre data simulada manualmente respetando los rangos del §2.

---

## 5. Checklist de entrega

- [ ] Archivo Java en `src/ur_os/process/planning/predictive/`.
- [ ] La clase implementa `ML_ModelEvaluator`.
- [ ] `shouldPrioritize` retorna en O(1), sin loops sobre datasets ni I/O.
- [ ] No introduce dependencias externas (Maven, Gradle, jars).
- [ ] Compila con `javac -sourcepath src src/ur_os/UR_OS.java` sin warnings nuevos.
- [ ] Incluye un comentario en cabecera con: versión del dataset usado, fecha del entrenamiento, accuracy reportada en validation.

---

*Documento mantenido por Juan Diego Susunaga (Fase 3). Última revisión: 2026-05-26.*
