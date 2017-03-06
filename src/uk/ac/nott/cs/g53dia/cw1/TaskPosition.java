package uk.ac.nott.cs.g53dia.cw1;

import uk.ac.nott.cs.g53dia.library.Task;

public class TaskPosition extends Position {

	public final Task task;

	public TaskPosition(Position position, Task task) {
		super(position.x, position.y);
		this.task = task;
	}

}
