# UR-OS: Predictive and Interactive Process Scheduler (ML)

**UR-OS** is an academic operating systems simulator developed in Java that allows the implementation of process scheduling, memory allocation, free memory management, and virtual memory algorithms.

> **Original Creator:** Pedro Wightman. Associate Professor at the School of Engineering, Science, and Technology at Universidad del Rosario, Bogota, Colombia.
> **Project Extension by:** Geronimo Rojas Guevara, Alejandra Contreras Carrillo, Mariana Sandoval Garzón, Juan Diego Susunaga Velasquez.

## 🚀 About the Project Extension
Unlike traditional approaches, this extended system integrates a lightweight **Machine Learning (ML)** model with a **"User Intent"** mechanism. 

Upon system startup, UR-OS interacts with the user to characterize their current workload, allowing the kernel to dynamically adjust its scheduling policies based on specific usage profiles (e.g., Development, Office, Multimedia), thereby globally optimizing efficiency and user experience.

## 📁 Project Architecture & Structure

To ensure scalability and separation of concerns, the project is structured as follows:

```text
Planificador-de-Procesos-Predictivo-e-Interactivo-ML-para-UR-OS/
├── src/ur_os/                           # Core Java Simulator (DO NOT mix with Python)
│   ├── memory/                          # Memory management (Paging, Segmentation, etc.)
│   ├── virtualmemory/                   # Virtual memory (Swap, Page replacement)
│   ├── system/                          # Hardware simulation (CPU, OS core)
│   ├── process/                         # Process and Instruction models
│   │   └── planning/                    # Process Scheduling Algorithms
│   │       ├── FCFS, SJF, RR...         # Classic algorithms
│   │       └── predictive/              # [NEW] ML-based scheduling (Phase 3)
│   └── telemetry/                       # [NEW] O(1) Data gathering (Phase 1)
│
├── ml_training/                         # [NEW] Machine Learning Environment (Phase 2)
│   ├── datasets/                        # Local CSV telemetry data
│   ├── models/                          # Exported models (JSON/Rules)
│   └── scripts/                         # Python training scripts (scikit-learn)
│
├── Docs/                                # Additional documentation (Roles, Git Workflow, Spanish translations)
├── .agentrules                          # AI Agent rules & Coding standards
└── .gitignore                           # Git ignore rules for Java & ML
```

## ⚙️ Development Phases
1. **Phase 1: Intent Interface and Telemetry** (Java)
   - User profile selection on boot.
   - $O(1)$ metric capture (CPU bursts, I/O blocks) inside the `telemetry` module.
2. **Phase 2: Multi-Profile Training** (Python)
   - Offline training of a Decision Tree using `scikit-learn` in the `ml_training` folder.
3. **Phase 3: Profile-Based Inference** (Java)
   - Translating the ML model to lightweight Java logic.
   - Implementing `ML_Scheduler` in the `predictive` package.
4. **Phase 4: Benchmarking**
   - Comparing ML-based scheduling against classic algorithms.

## 🛠️ Built With
- **Core Simulator:** Java (JDK)
- **Machine Learning:** Python, Scikit-learn, Pandas
- **Build System:** Ant (`build.xml`)

---
*Enjoy using and extending UR-OS!*
