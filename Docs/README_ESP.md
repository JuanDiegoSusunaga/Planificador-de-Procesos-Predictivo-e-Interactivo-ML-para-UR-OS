# UR-OS: Planificador de Procesos Predictivo e Interactivo (ML)

**UR-OS** es un simulador académico de sistemas operativos desarrollado en Java que permite la implementación de algoritmos de planificación de procesos, asignación de memoria, gestión de memoria libre y memoria virtual.

> **Creador Original:** Pedro Wightman. Profesor Asociado en la Escuela de Ingeniería, Ciencia y Tecnología de la Universidad del Rosario, Bogotá, Colombia.
> **Extensión del Proyecto por:** Gerónimo Rojas Guevara, Alejandra Contreras Carrillo, Mariana Sandoval Garzón, Juan Diego Susunaga Velásquez.

## 🚀 Acerca de la Extensión del Proyecto
A diferencia de los enfoques tradicionales, este sistema extendido integra un modelo ligero de **Machine Learning (ML)** con un mecanismo de **"User Intent" (Intención del Usuario)**. 

Al iniciar el sistema, UR-OS interactúa con el usuario para caracterizar su carga de trabajo actual, permitiendo que el kernel ajuste dinámicamente sus políticas de planificación en función de perfiles de uso específicos (ej. Desarrollo, Oficina, Multimedia), optimizando así la eficiencia y la experiencia del usuario a nivel global.

## 📁 Arquitectura y Estructura del Proyecto

Para asegurar la escalabilidad y la separación de responsabilidades, el proyecto está estructurado de la siguiente manera:

```text
Planificador-de-Procesos-Predictivo-e-Interactivo-ML-para-UR-OS/
├── src/ur_os/                           # Simulador Base en Java (NO mezclar con Python)
│   ├── memory/                          # Gestión de memoria (Paginación, Segmentación, etc.)
│   ├── virtualmemory/                   # Memoria virtual (Swap, Reemplazo de páginas)
│   ├── system/                          # Simulación de Hardware (CPU, OS core)
│   ├── process/                         # Modelos de Procesos e Instrucciones
│   │   └── planning/                    # Algoritmos de Planificación de Procesos
│   │       ├── FCFS, SJF, RR...         # Algoritmos clásicos
│   │       └── predictive/              # [NUEVO] Planificación basada en ML (Fase 3)
│   └── telemetry/                       # [NUEVO] Recolección de datos O(1) (Fase 1)
│
├── ml_training/                         # [NUEVO] Entorno de Machine Learning (Fase 2)
│   ├── datasets/                        # Datos locales de telemetría en CSV
│   ├── models/                          # Modelos exportados (JSON/Reglas)
│   └── scripts/                         # Scripts de entrenamiento en Python (scikit-learn)
│
├── Docs/                                # Documentación adicional (Roles, Git Workflow, Propuesta)
├── .agentrules                          # Reglas para Agentes de IA y estándares de código
└── .gitignore                           # Reglas para ignorar archivos en Git (Java y ML)
```

## ⚙️ Fases de Desarrollo
1. **Fase 1: Interfaz de Intención y Telemetría** (Java)
   - Selección de perfil de usuario en el arranque.
   - Captura de métricas $O(1)$ (Ráfagas de CPU, bloqueos de E/S) dentro del módulo `telemetry`.
2. **Fase 2: Entrenamiento Multi-Perfil** (Python)
   - Entrenamiento offline de un Árbol de Decisión usando `scikit-learn` en la carpeta `ml_training`.
3. **Fase 3: Inferencia Basada en Perfiles** (Java)
   - Traducción del modelo de ML a lógica ligera en Java.
   - Implementación de `ML_Scheduler` en el paquete `predictive`.
4. **Fase 4: Benchmarking y Evaluación**
   - Comparación de la planificación basada en ML frente a algoritmos clásicos.

## 🛠️ Tecnologías Utilizadas
- **Simulador Core:** Java (JDK)
- **Machine Learning:** Python, Scikit-learn, Pandas
- **Sistema de Construcción:** Ant (`build.xml`)

---
*¡Disfruta usando y extendiendo UR-OS!*
