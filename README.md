# Architectural Stability Reasoning using Self-Awareness Principles

> **Replication package for the paper:**  
> *Architectural Stability Reasoning using Self-Awareness Principles: Case of Self-Adaptive Cloud Architectures*  
> Submitted to the *Journal of Systems and Software* (JSS), Ref. No.: JSSOFTWARE-D-26-00416

---

## Overview

This repository contains the source code, experimental data, models, and analysis scripts accompanying the above paper. The paper proposes a framework for reasoning about the **behavioural stability** of self-adaptive cloud architectures at runtime, using three self-awareness capabilities:

| Capability | Mechanism | Architecture label |
|---|---|---|
| Goal-Awareness | SAwGoals@run.time runtime goal modelling | SAwArch\_G |
| Time-Awareness | Q-learning (online reinforcement learning) | SAwArch\_T |
| Meta-Self-Awareness | Stochastic multi-player games (PRISM-games) | SAwArch\_M |
| Baseline (no self-awareness) | Reactive MAPE-K (stimulus-awareness) | SAwArch\_S |

Stability is evaluated across three attributes — **response time**, **energy consumption** and **operational cost** — 
under realistic workload variations drawn from the WorldCup 98 dataset  — 
across five service types (S0–S4).

---

## Repository Structure

```
.
├── simulation/             # CloudSim-based simulation implementation (Java JDK 1.8)
│   ├── core/               # Core architecture and MAPE-K loop components
│   ├── awareness/          # Self-awareness capability implementations
│   │   ├── goal/           # SAwGoals@run.time goal modelling component
│   │   ├── time/           # Q-learning online learning component
│   │   └── meta/           # Meta-self-awareness integration layer
│   └── tactics/            # Tactic catalogue and executor
│
├── experiments/
│   ├── experiments/        # Experiment configuration and input files
│   ├── workload/           # WorldCup 98 workload trace and MIPS-mapped service data
│   └── results/            # Raw experimental results for all four architectures × five services
│
└── 
```

---

## Requirements

### Simulation

- **Java JDK 1.8** or later
- **CloudSim 3.x** — cloud simulation toolkit ([Calheiros et al., 2011](https://doi.org/10.1002/spe.995))
- Maven (for dependency management)

### Probabilistic model checking

- **PRISM-games 2.0** (beta 3) — available at [prismmodelchecker.org](https://www.prismmodelchecker.org/games/)
- macOS 10.13+ or equivalent Linux environment

---

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/<your-username>/<repo-name>.git
cd <repo-name>
```

### 2. Build the simulation

```bash
cd simulation
mvn clean install
```

### 3. Run the experiments

Experiment files are provided in the `experiments` package. 
Each architectural configuration can be run independently: `stimulus-awareness` (baseline), `goal-awareness`, `time-awareness`, and `meta-self-awareness`, across service types S0–S4. 
Results will be saved automatically to the `results` package.

---

## Experimental Configuration

| Parameter | Value |
|---|---|
| Simulation platform | CloudSim 3.x |
| Benchmark | RUBiS (online auction application) |
| Workload trace | WorldCup 98 |
| Initial hosts | 10 × IBM x3550 (2 × Xeon X5675, 6 cores, 256 GB RAM) |
| Maximum host capacity | 1,000 hosts |
| VM types | Amazon EC2 m4.large, m4.xlarge, m4.2xlarge |
| Stability objectives | Response time: 25 ms · Energy: 25 kWh · Cost: $50 |
| Q-learning parameters | α = 1 · γ = 0.8 |
| PRISM-games version | 2.0 beta 3 |

---

## Citation

If you use this code, data, or models in your research, please cite:

```bibtex
@article{<citekey>,
  title   = {Architectural Stability Reasoning using Self-Awareness Principles:
             Case of Self-Adaptive Cloud Architectures},
  author  = {<Your Name> and Bahsoon, Rami and Buyya, Rajkumar},
  journal = {Journal of Systems and Software},
  year    = {<year>},
  note    = {Under review. Ref.\ No.: JSSOFTWARE-D-26-00416}
}
```

---

## Licence

This repository is released under the [Creative Commons Attribution 4.0 International Licence (CC BY 4.0)](https://creativecommons.org/licenses/by/4.0/). You are free to share and adapt the material for any purpose, provided appropriate credit is given to the authors.

---

## Contact

For questions regarding the paper or the replication package, please open an issue in this repository or contact the corresponding author.
