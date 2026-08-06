package de.djhub;

public class Feature {

	public final String id;
	public final String name;
	public final String description;
	public final String category;
	public final boolean defaultValue;
	/** false = wird nicht gespeichert (z.B. der AutoMiner-Laufzustand). */
	public final boolean persistent;

	private boolean enabled;

	public Feature(String id, String name, String description, String category,
			boolean defaultValue, boolean persistent) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.category = category;
		this.defaultValue = defaultValue;
		this.persistent = persistent;
		this.enabled = defaultValue;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void toggle() {
		this.enabled = !this.enabled;
	}
}
