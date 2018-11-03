package slenderMan;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;

import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import saf.v3d.scene.VSpatial;

public class Style extends DefaultStyleOGL2D {
	@Override
	public String getLabel(Object object) {
		if (object.getClass() == Device.class)
			return ((Device) object).getID() + "";
		return null;
	}

	@Override
	public Font getLabelFont(Object object) {
		return new Font(Font.MONOSPACED, Font.PLAIN, 26);
	}

	@Override
	public VSpatial getVSpatial(Object agent, VSpatial spatial) {
		if (agent.getClass() == Player.class && ((Player) agent).needsToUpdateStyle()) {
			spatial = null;
			((Player) agent).setUpdateStyle(false);
		}
		if (spatial == null) {
			if (agent.getClass() == Slender.class) {
				try {
					spatial = shapeFactory.createImage("src/imgs/Slendy.png", 0.08f);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (agent.getClass() == Player.class) {
				if (((Player) agent).isStartingRunning()) {
					try {
						spatial = shapeFactory.createImage("src/imgs/player_running.png", 0.06f);
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else if (((Player) agent).isMobileOn() || ((Player) agent).isSendingMsg()) {
					try {
						spatial = shapeFactory.createImage("src/imgs/player_mobile.png", 0.06f);
						((Player) agent).setSendingMsg(false);
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					try {
						spatial = shapeFactory.createImage("src/imgs/player_walking.png", 0.06f);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} else if (agent.getClass() == Device.class) {
				spatial = shapeFactory.createCircle(8, 16);
			} else if (agent.getClass() == Recharge.class) {
				try {
					spatial = shapeFactory.createImage("src/imgs/recharger.png", 0.25f);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return spatial;
	}

	@Override
	public Color getColor(Object agent) {
		if (agent.getClass() == Slender.class) {
			return Color.BLACK;
		} else if (agent.getClass() == Player.class) {
			return Color.PINK;
		} else if (agent.getClass() == Device.class && ((Device) agent).isOn()) {
			return Color.LIGHT_GRAY;
		} else if (agent.getClass() == Recharge.class) {
			return Color.YELLOW;
		}
		return Color.CYAN;
	}

}
