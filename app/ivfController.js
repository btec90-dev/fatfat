/**
 * ivfController.js
 * Manages state persistence, editing modals, custom milestone additions,
 * and gestational age/due date calculations for blastocyst transfer.
 */

const STORAGE_KEY = 'ivf_protocol_data_v1';
const PREGNANCY_SYNC_KEY = 'pregnancy_active_sync_v1';

export const initialIvfData = {
  phases: [
    {
      id: "phase-1",
      title: "Phase 1: Stimulation & Retrieval",
      status: "Completed",
      events: [
        {
          id: "evt-1-1",
          date: "2026-09-19",
          time: "",
          title: "Ovarian suppression initiated",
          instructions: "Nortrel daily",
          location: "",
          status: "Completed"
        },
        {
          id: "evt-1-2",
          date: "2026-09-21",
          time: "",
          title: "Ovarian stimulation initiated",
          instructions: "Gonal-F 150 IU + Menopur 150 IU",
          location: "",
          status: "Completed"
        },
        {
          id: "evt-1-3",
          date: "2026-09-23",
          time: "07:00",
          title: "Monitoring Visit 1",
          instructions: "Bloodwork & Ultrasound",
          location: "RMA of New York",
          status: "Completed"
        },
        {
          id: "evt-1-4",
          date: "2026-09-25",
          time: "",
          title: "Antagonist added",
          instructions: "Cetrotide 0.25 mg daily",
          location: "",
          status: "Completed"
        },
        {
          id: "evt-1-5",
          date: "2026-09-27",
          time: "07:30",
          title: "Monitoring Visit 2",
          instructions: "Bloodwork & Ultrasound check",
          location: "RMA of New York",
          status: "Completed"
        },
        {
          id: "evt-1-6",
          date: "2026-09-29",
          time: "23:00",
          title: "Trigger injection",
          instructions: "Administered precisely at designated trigger time",
          location: "Home",
          status: "Completed"
        },
        {
          id: "evt-1-7",
          date: "2026-09-30",
          time: "08:45",
          title: "Pre-retrieval lab check",
          instructions: "Confirm trigger absorption & baseline progesterone",
          location: "RMA of New York",
          status: "Completed"
        },
        {
          id: "evt-1-8",
          date: "2026-10-01",
          time: "07:30",
          title: "Egg Retrieval Procedure",
          instructions: "Completed under conscious sedation. 42 oocytes retrieved.",
          location: "RMA Surgical Suite",
          status: "Completed"
        }
      ]
    },
    {
      id: "phase-2",
      title: "Phase 2: Embryology & Lab Results",
      status: "Completed",
      events: [
        {
          id: "evt-2-1",
          date: "2026-10-02",
          time: "",
          title: "ICSI Fertilization Report",
          instructions: "42 retrieved -> 34 mature -> 11 successfully fertilized",
          location: "Embryology Lab",
          status: "Completed"
        },
        {
          id: "evt-2-2",
          date: "2026-10-06",
          time: "",
          title: "Blastocyst Culture & Trophectoderm Biopsy",
          instructions: "Developing blastocysts biopsied successfully",
          location: "Embryology Lab",
          status: "Completed"
        },
        {
          id: "evt-2-3",
          date: "2026-10-09",
          time: "",
          title: "Laboratory Yield",
          instructions: "8 blastocysts biopsied and vitrified",
          location: "Embryology Lab",
          status: "Completed"
        },
        {
          id: "evt-2-4",
          date: "2026-10-11",
          time: "09:15",
          title: "Baseline FET Ultrasound / Labs",
          instructions: "Baseline check and Nortrel resumed",
          location: "RMA of New York",
          status: "Completed"
        },
        {
          id: "evt-2-5",
          date: "2026-10-13",
          time: "",
          title: "Thaw Plan & Embryo Selection",
          instructions: "Selected: Male euploid embryo",
          location: "Clinic",
          status: "Completed"
        },
        {
          id: "evt-2-6",
          date: "2026-10-25",
          time: "",
          title: "PGT-A Results Received",
          instructions: "5 euploid / normal embryos confirmed",
          location: "Genetic Lab",
          status: "Completed"
        }
      ]
    },
    {
      id: "phase-3",
      title: "Phase 3: Surgical Prep & Uterine Optimization",
      status: "Completed",
      events: [
        {
          id: "evt-3-1",
          date: "2026-10-15",
          time: "",
          title: "Initial Assessment",
          instructions: "Pre-surgical uterine assessment and planning",
          location: "Clinic",
          status: "Completed"
        },
        {
          id: "evt-3-2",
          date: "2026-10-28",
          time: "06:15",
          title: "Operative Hysteroscopy",
          instructions: "Debris removal completed successfully",
          location: "Gramercy Surgery Center (380 2nd Ave, Suite 1000, 10th Fl, New York, NY 10010)",
          status: "Completed"
        }
      ]
    },
    {
      id: "phase-4",
      title: "Phase 4: Endometrial Preparation & FET",
      status: "Active / In Progress",
      events: [
        {
          id: "evt-4-1",
          date: "2026-11-05",
          time: "",
          title: "Baseline Monitoring",
          instructions: "Bloodwork, Ultrasound & Doctor Consultation completed",
          location: "Clinic",
          status: "Completed"
        },
        {
          id: "evt-4-2",
          date: "2026-11-06",
          time: "",
          title: "Estrogen Protocol (Days 1–4)",
          instructions: "Oral estrogen pills 2x daily (Nov 6–9)",
          location: "Home",
          status: "In Progress"
        },
        {
          id: "evt-4-3",
          date: "2026-11-10",
          time: "",
          title: "Estrogen Protocol Increase (Day 5 onward)",
          instructions: "Increase to 3x daily",
          location: "Home",
          status: "Pending"
        },
        {
          id: "evt-4-4",
          date: "2026-11-12",
          time: "08:30",
          title: "Clinic Monitoring Appointment",
          instructions: "Endometrial thickness & bloodwork check",
          location: "Clinic",
          status: "Pending"
        },
        {
          id: "evt-4-5",
          date: "2026-11-20",
          time: "11:00",
          title: "Frozen Embryo Transfer (FET)",
          instructions: "Transfer of 1 male euploid blastocyst (Target date pending lining check)",
          location: "Clinic",
          status: "Pending"
        }
      ]
    }
  ]
};

export class IvfController {
  constructor() {
    this.data = this.loadData();
  }

  loadData() {
    try {
      const saved = localStorage.getItem(STORAGE_KEY);
      if (saved) {
        return JSON.parse(saved);
      }
    } catch (e) {
      console.error("Error loading IVF data from localStorage:", e);
    }
    return initialIvfData;
  }

  saveData() {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(this.data));
    } catch (e) {
      console.error("Error saving IVF data to localStorage:", e);
    }
  }

  updateEvent(phaseId, eventId, updatedFields) {
    for (let phase of this.data.phases) {
      if (phase.id === phaseId) {
        for (let evt of phase.events) {
          if (evt.id === eventId) {
            Object.assign(evt, updatedFields);
            this.saveData();
            this.checkPregnancySync(evt);
            return true;
          }
        }
      }
    }
    return false;
  }

  addCustomMilestone(phaseId, eventObj) {
    for (let phase of this.data.phases) {
      if (phase.id === phaseId) {
        const newEvent = {
          id: `evt-custom-${Date.now()}`,
          date: eventObj.date || new Date().toISOString().split('T')[0],
          time: eventObj.time || "",
          title: eventObj.title || "Custom Milestone",
          instructions: eventObj.instructions || "",
          location: eventObj.location || "",
          status: eventObj.status || "Pending"
        };
        phase.events.push(newEvent);
        this.saveData();
        return newEvent;
      }
    }
    return null;
  }

  calculatePregnancySync(transferDateStr) {
    // Blastocyst Day 5/6 transfer formula:
    // LMP = Transfer Date - 19 days
    // EDD = Transfer Date + 266 days
    const transferDate = new Date(transferDateStr);
    if (isNaN(transferDate)) return null;

    const lmpDate = new Date(transferDate.getTime() - 19 * 24 * 3600 * 1000);
    const eddDate = new Date(transferDate.getTime() + 266 * 24 * 3600 * 1000);

    const now = new Date();
    const diffMs = now - lmpDate;
    const totalDays = Math.max(0, Math.floor(diffMs / (24 * 3600 * 1000)));
    const weeks = Math.floor(totalDays / 7);
    const days = totalDays % 7;

    const syncResult = {
      transferDate: transferDateStr,
      lmpDate: lmpDate.toISOString().split('T')[0],
      eddDate: eddDate.toISOString().split('T')[0],
      gestationalAgeWeeks: weeks,
      gestationalAgeDays: days,
      calculatedAt: Date.now()
    };

    localStorage.setItem(PREGNANCY_SYNC_KEY, JSON.stringify(syncResult));
    return syncResult;
  }

  checkPregnancySync(event) {
    if (
      (event.title.toLowerCase().includes('transfer') || event.title.toLowerCase().includes('fet')) &&
      event.status.toLowerCase() === 'completed'
    ) {
      return this.calculatePregnancySync(event.date);
    }
    return null;
  }
}
