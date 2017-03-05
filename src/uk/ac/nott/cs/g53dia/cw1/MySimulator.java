package uk.ac.nott.cs.g53dia.cw1;

import javax.swing.WindowConstants;

import uk.ac.nott.cs.g53dia.library.Action;
import uk.ac.nott.cs.g53dia.library.ActionFailedException;
import uk.ac.nott.cs.g53dia.library.Cell;
import uk.ac.nott.cs.g53dia.library.Environment;
import uk.ac.nott.cs.g53dia.library.OutOfFuelException;
import uk.ac.nott.cs.g53dia.library.Tanker;
import uk.ac.nott.cs.g53dia.library.TankerViewer;

public class MySimulator {

	private static final int DELAY = 100;
	private static final int DURATION = 10000;
	
	public static void main(String[] args) {

        Environment env = new Environment(Tanker.MAX_FUEL/2);

        Tanker t = new MyTanker();

        TankerViewer tv = new TankerViewer(t, new MyTankerViewerIconFactory());
        tv.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        while (env.getTimestep() < DURATION) {

            env.tick();
            tv.tick(env);

            Cell[][] view = env.getView(t.getPosition(), Tanker.VIEW_RANGE);
            Action a = t.senseAndAct(view, env.getTimestep());

            try {
                a.execute(env, t);
            } catch (OutOfFuelException e) {
                System.out.println("Tanker out of fuel!");
                break;
            } catch (ActionFailedException e) {
                System.err.println("Failed: " + e.getMessage());
            }
            try { Thread.sleep(DELAY);} catch (Exception ignored) { }
        }
    }

	
}
