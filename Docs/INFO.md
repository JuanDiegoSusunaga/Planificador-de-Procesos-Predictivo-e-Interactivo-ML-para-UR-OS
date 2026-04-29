# Project Proposal: Predictive and Interactive Process Scheduler (ML) for UR-OS

**Team Members:**
- Geronimo Rojas Guevara
- Alejandra Contreras Carrillo
- Mariana Sandoval Garzón
- Juan Diego Susunaga Velasquez

## 1. Project Summary
This project proposes the design and implementation of a Predictive and Interactive Process Scheduler for the UR-OS operating system. Unlike traditional approaches, this system integrates a lightweight Machine Learning (ML) model with a "User Intent" mechanism. Upon system startup, UR-OS will interact with the user to characterize their current workload, allowing the kernel to dynamically adjust its scheduling policies based on specific usage profiles (e.g., development, office, multimedia), thereby globally optimizing efficiency and user experience.

## 2. Purpose and Justification
In modern systems such as Windows 11 or various Linux distributions, resource management is not static; it adapts to whether the user is gaming, programming, or saving battery. This project seeks to bridge the gap between academic operating systems theory and current industrial implementation through:
- **Low-level Interactivity:** Implementing an interface where the system "asks" the user for their work profile.
- **Contextual Resource Management:** Applying differentiated policies according to the selected work area.
- **Innovation in UR-OS:** Evolving the kernel from a reactive model to a proactive one based on machine learning.

## 3. User Profile Characterization
To comply with the "User Intent" approach, the operating system will offer the following profiles at the start of the session:

| User Profile | Predominant Workload | Scheduler Strategy |
| :--- | :--- | :--- |
| **Development / Compilation** | CPU Intensive (Calculations, code compilation). | Longer time quantums to minimize context switches. |
| **Office / Browsing** | I/O Intensive (Keyboard, network, file reading). | High priority for interactive processes to reduce perceived latency. |
| **Multimedia / Gaming** | Mixed (Thread synchronization and fast memory access). | Resource isolation for the main foreground process. |

## 4. Technical Methodology
The development will be structured in four phases adapted to integrate user feedback into the ML decision-making process:

**Phase 1: Intent Interface and Telemetry**
Upon booting UR-OS, a review module will execute and ask the user which area they will be working in. This choice will be stored as a global state variable in the kernel. Simultaneously, the Process Control Block (PCB) will capture metrics of CPU bursts and I/O block frequencies, tagging them with the active profile.

**Phase 2: Multi-Profile Training (Offline)**
A dataset in CSV format will be generated correlating process behavior with the user-selected profile. Using Python and Scikit-learn, a Decision Tree will be trained, capable of predicting whether a process should be prioritized or penalized given a specific context of system use.

**Phase 3: Profile-Based Inference (Kernel)**
The ML model will be translated into optimized conditional logic in Java/C++. The UR-OS Scheduler will consult both real-time process telemetry and the global "User Intent" variable to make scheduling decisions in milliseconds.

**Phase 4: Benchmarking and Evaluation**
The system's performance will be compared under different profiles. It will evaluate whether the "Office" profile truly improves the response of interactive applications compared to a standard algorithm like Round Robin, using response time and resource utilization graphs.

## 5. Syllabus Alignment

| Activity | Weight | Relationship with the Project |
| :--- | :--- | :--- |
| **Class Project (UR-OS Challenge)** | 25% | Delivery of the optimized predictive and interactive scheduler. |
| **First Evaluation** | 20% | Development of the user interface and baseline telemetry capture. |

---
*This document updates the initial proposal by integrating a user profile-based scheduling approach for the Operating Systems (11310079) course, Universidad del Rosario.*
