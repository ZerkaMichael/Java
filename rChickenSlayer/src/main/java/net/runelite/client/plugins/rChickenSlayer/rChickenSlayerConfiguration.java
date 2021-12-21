package net.runelite.client.plugins.rChickenSlayer;

import net.runelite.client.config.*;

@ConfigGroup("rChickenSlayer")
public interface rChickenSlayerConfiguration extends Config {
	@ConfigTitle(
			keyName = "coordsTitle",
			name = "Custom Coordinate Settings",
			description = "",
			position = 2
	)
	String coordsTitle = "coordsTitle";

	@ConfigItem(
			keyName = "customChickenLocation",
			name = "Custom Chicken Location",
			description = "Enter the location you want to fight Chickens at (x,y,z)",
			position = 4,
			title = "Custom Locations"

	)
	default String customChickenLocation() { return "3228,3297,0"; }

	@ConfigItem(
			keyName = "startButton",
			name = "Start",
			description = "",
			position = 6,
			section = "Controls"
	)
	default Button startButton() { return new Button(); }

	@ConfigItem(
			keyName = "stopButton",
			name = "Stop",
			description = "",
			position = 7,
			section = "Controls"
	)
	default Button stopButton() { return new Button(); }
}
