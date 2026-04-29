# Flujo de Trabajo en Git y Estrategia de Ramas

Para mantener al equipo organizado y evitar conflictos (merge conflicts) entre el código de Java y el de Python, este proyecto sigue un modelo de **Ramas por Funcionalidad (Feature Branch Workflow)**.

## Ramas Principales
- `main`: La versión estable del proyecto. El código aquí siempre debe compilar y ejecutarse sin errores.
- `develop`: La rama activa de integración. Todas las funcionalidades terminadas se fusionan aquí primero antes de hacer una entrega final a `main`.

## Ramas de Funcionalidad (Feature Branches)
Siempre que un miembro del equipo inicie una nueva tarea, debe crear una rama a partir de `develop`. La convención de nombres es:
`feature/faseX-descripcion-corta`

### Ramas Recomendadas para este Proyecto:
1. **`feature/fase1-telemetry`**: Usada por el *Ingeniero de Telemetría* para agregar la recolección de métricas O(1) en el núcleo del OS.
2. **`feature/fase1-user-intent`**: Usada por el *Ingeniero de UI* para construir el menú de selección de perfil al iniciar.
3. **`feature/fase2-ml-training`**: Usada por el *Científico de Datos* para subir los scripts de Python (Recuerda: NO subir archivos CSV pesados ni modelos `.pkl` aquí).
4. **`feature/fase3-ml-scheduler`**: Usada por el *Arquitecto del Planificador* para construir la lógica predictiva en Java.
5. **`feature/fase4-benchmarking`**: Usada para crear los escenarios de prueba y los scripts de evaluación final.

## Pull Requests (PRs)
1. Nunca hagas "push" directamente a `main` o `develop`.
2. Cuando termines una funcionalidad, abre un Pull Request hacia `develop`.
3. Al menos otro miembro del equipo debe revisar tu código antes de fusionarlo (merge) para asegurar que cumple con las `.agentrules`.
