package uk.ac.nott.cs.g53dia.cw1;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import uk.ac.nott.cs.g53dia.library.Cell;
import uk.ac.nott.cs.g53dia.library.FuelPump;
import uk.ac.nott.cs.g53dia.library.Station;
import uk.ac.nott.cs.g53dia.library.Tanker;
import uk.ac.nott.cs.g53dia.library.TankerViewerIconFactory;
import uk.ac.nott.cs.g53dia.library.Well;

public class MyTankerViewerIconFactory implements TankerViewerIconFactory {

	private static final ImageIcon tanker;
	private static final ImageIcon pump;
	private static final ImageIcon well;
	private static final ImageIcon stationIdle;
	private static final ImageIcon stationTask;

	static {
		tanker = createImageIcon("img/tanker.png");
		pump = createImageIcon("img/pump.png");
		well = createImageIcon("img/well.png");
		stationIdle = createImageIcon("img/stationIdle.png");
		stationTask = createImageIcon("img/stationTask.png");
	}

	private static ImageIcon createImageIcon(String path) {
		java.net.URL img = MyTankerViewerIconFactory.class.getResource(path);
		if (img != null) {
			return new ImageIcon(img);
		} else {
			System.err.println("Couldn't load image: " + path);
			return null;
		}
	}


	@Override
	public Icon getIconForCell(Cell cell) {
		if (cell == null) return null;

		if (cell instanceof FuelPump) return pump;
		else if (cell instanceof Well) return well;
		else if (cell instanceof Station) {
			if (((Station) cell).getTask() != null) return stationTask;
			else return stationIdle;
		}
		else return null;
	}

	@Override
	public Icon getIconForTanker(Tanker tankerObj) {
		return tanker;
	}

	@Override
	public int iconSize() {
		return 16;
	}
}
