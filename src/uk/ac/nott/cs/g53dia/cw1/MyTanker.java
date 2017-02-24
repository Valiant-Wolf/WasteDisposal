package uk.ac.nott.cs.g53dia.cw1;

import uk.ac.nott.cs.g53dia.library.Action;
import uk.ac.nott.cs.g53dia.library.Cell;
import uk.ac.nott.cs.g53dia.library.FuelPump;
import uk.ac.nott.cs.g53dia.library.Tanker;

public class MyTanker extends Tanker{

	private Position position = new Position(0, 0);
	private DualRunnerList<Position> fuelList = new DualRunnerList<>();

	@Override
	public Action senseAndAct(Cell[][] view, long timeStep) {
		discoverFuel(view);
		return null;
	}

	private void discoverFuel(Cell[][] view) {
		for (int ix = 0; ix < view.length; ix++) {
			for (int iy = 0; iy < view[0].length; iy++) {
				Cell cell = view[ix][iy];
				if (cell instanceof FuelPump) {
					Position cellPos = new Position(
							position.x - (ix - VIEW_RANGE),
							position.y - (iy - VIEW_RANGE)
					);
					fuelList.add(cellPos.x, cellPos.y, cellPos);
				}
			}
		}
	}

	private void moveTowardsPosition(Position target) {
		byte byteDirection = 0;

		if (position.y < target.y) {
			position.y++;
			byteDirection &= 0x1;
		}
		if (position.y > target.y) {
			position.y--;
			byteDirection &= 0x2;
		}
		if (position.x < target.x) {
			position.x++;
			byteDirection &= 0x4;
		}
		if (position.x > target.x) {
			position.x--;
			byteDirection &= 0x8;
		}

		fuelList.moveTo(position.x, position.y);
	}

	private class Position {

		int x;
		int y;

		Position(int x, int y) {
			this.x = x;
			this.y = y;
		}

		int distanceTo(Position that) {
			int dx = Math.abs(that.x - this.x);
			int dy = Math.abs(that.y - this.y);
			return Math.max(dx, dy);
		}

	}

}
