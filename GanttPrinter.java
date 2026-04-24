/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author johnh
 */
// GanttPrinter.java
import java.util.List;

public class GanttPrinter {

    public static void print(List<GanttEntry> gantt) {
        if (gantt == null || gantt.isEmpty()) {
            System.out.println("Gantt Chart: (empty)");
            return;
        }

        System.out.println("\nGantt Chart:");

        // ── Top border ──────────────────────────────────────
        System.out.print("+");
        for (GanttEntry e : gantt) {
            int width = Math.max((e.endTime - e.startTime) * 3, e.pid.length() + 2);
            System.out.print("-".repeat(width) + "+");
        }
        System.out.println();

        // ── Process labels ───────────────────────────────────
        System.out.print("|");
        for (GanttEntry e : gantt) {
            int width = Math.max((e.endTime - e.startTime) * 3, e.pid.length() + 2);
            int pad   = width - e.pid.length();
            int left  = pad / 2;
            int right = pad - left;
            System.out.print(" ".repeat(left) + e.pid + " ".repeat(right) + "|");
        }
        System.out.println();

        // ── Bottom border ────────────────────────────────────
        System.out.print("+");
        for (GanttEntry e : gantt) {
            int width = Math.max((e.endTime - e.startTime) * 3, e.pid.length() + 2);
            System.out.print("-".repeat(width) + "+");
        }
        System.out.println();

        // ── Time markers ─────────────────────────────────────
        for (GanttEntry e : gantt) {
            int width = Math.max((e.endTime - e.startTime) * 3, e.pid.length() + 2);
            String t  = String.valueOf(e.startTime);
            System.out.print(t + " ".repeat(Math.max(0, width - t.length())));
        }
        System.out.println(gantt.get(gantt.size() - 1).endTime);
    }
}