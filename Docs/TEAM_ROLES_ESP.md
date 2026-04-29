# Roles del Equipo y División de Tareas

Este documento describe los cuatro roles principales necesarios para completar el proyecto del Planificador de Procesos Predictivo e Interactivo. Los miembros del equipo pueden asignar sus nombres al rol que mejor se adapte a sus habilidades e intereses.

## 1. 🟢 El Ingeniero de Telemetría (Core de Java)
**Responsable:** `[Sin asignar]`
**Fase de Enfoque:** Fase 1 (Recolección de Datos)
**Responsabilidades:**
- Integrarse en las clases principales del OS (`SystemOS.java`, `Process.java`, `CPU.java`).
- Implementar `TelemetryManager.java` para contar las ráfagas de CPU y los bloqueos de E/S con una complejidad de tiempo $O(1)$.
- Asegurar que los datos de telemetría se formateen y escriban correctamente en un archivo CSV sin causar cuellos de botella en el simulador de Java.
**Habilidades Recomendadas:** Java avanzado, buena comprensión de los estados de un sistema operativo y las colas.

## 2. 🧠 El Científico de Datos (Ingeniero ML)
**Responsable:** `Mariana Sandoval`
**Fase de Enfoque:** Fase 2 (Machine Learning)
**Responsabilidades:**
- Trabajar exclusivamente en el directorio de Python `ml_training/`.
- Ingerir los datasets en CSV generados por el Ingeniero de Telemetría utilizando `pandas`.
- Entrenar un clasificador de Árbol de Decisión usando `scikit-learn` para predecir si un proceso debe ser priorizado basado en el Perfil del Usuario.
- Exportar la lógica del modelo entrenado a un formato ligero y fácilmente legible (ej. código Java de `if-else` generado automáticamente, o JSON) para la Fase 3.
**Habilidades Recomendadas:** Python, Pandas, Scikit-learn, Análisis de Datos.

## 3. ⚙️ El Arquitecto del Planificador (Integrador ML en Java)
**Responsable:** `[Sin asignar]`
**Fase de Enfoque:** Fase 3 (Planificación Predictiva)
**Responsabilidades:**
- Crear la clase `ML_Scheduler.java` dentro del paquete `predictive`.
- Analizar e integrar las reglas de ML generadas por el Científico de Datos en el Planificador de Java.
- Asegurar que el planificador pueda leer la variable global "User Intent" y aplicar la lógica de ML en milisegundos durante el Cambio de Contexto (Context Switch).
**Habilidades Recomendadas:** Java avanzado, algoritmos, comprensión de la planificación de CPU (Round Robin, FCFS).

## 4. 📊 El Ingeniero de QA y Benchmarking (UI y Analítica)
**Responsable:** `[Sin asignar]`
**Fase de Enfoque:** Fase 1 (UI) y Fase 4 (Evaluación)
**Responsabilidades:**
- Implementar la clase `UserIntentInterface.java` que solicita al usuario su perfil (Desarrollo, Oficina, Multimedia) durante la secuencia de arranque de UR-OS.
- Diseñar escenarios de prueba para ejecutar el simulador bajo cargas pesadas.
- Comparar el nuevo `ML_Scheduler` contra los algoritmos clásicos.
- Generar gráficos visuales (Tiempo de Respuesta vs. Utilización de Recursos) que demuestren el éxito del proyecto.
**Habilidades Recomendadas:** Java (para la interfaz de arranque), Python/Matplotlib/Excel (para graficar los resultados del benchmarking), habilidades analíticas.
