package io.rapid.rapido.ui.filter;

/**
 * Representation of filtering tasks based on current state they are in
 */
public enum FilterState {
	ALL, // Show all tasks
	DONE, // Show only tasks with done==true
	NOT_DONE // Show only tasks with done==false
}
