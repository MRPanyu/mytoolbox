package mrpanyu.mytoolbox.framework.utils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class ScreenCapture {

	public static void capture(ScreenCaptureCallback callback) {
		ScreenCaptureFrame f = new ScreenCaptureFrame();
		try {
			f.init(callback);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static interface ScreenCaptureCallback {
		void onCaptured(BufferedImage img);

		void onCancel();
	}

	@SuppressWarnings("serial")
	private static class ScreenCaptureFrame extends JFrame {

		private ScreenCaptureCallback callback;

		private int screenWidth;
		private int screenHeight;
		private BufferedImage capturedImage;
		private JLabel imageLabel;

		private int startX = -1;
		private int startY = -1;
		private int endX = -1;
		private int endY = -1;

		public void init(ScreenCaptureCallback callback) throws Exception {
			this.callback = callback;
			Robot robot = new Robot();
			GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			screenWidth = gd.getDisplayMode().getWidth();
			screenHeight = gd.getDisplayMode().getHeight();
			capturedImage = robot.createScreenCapture(new Rectangle(0, 0, screenWidth, screenHeight));
			imageLabel = new JLabel(new ImageIcon(capturedImage));
			imageLabel.setPreferredSize(new Dimension(screenWidth, screenHeight));
			imageLabel.addMouseListener(new SCMouseListener());
			imageLabel.addMouseMotionListener(new SCMouseMotionListener());
			this.add(imageLabel);
			this.setUndecorated(true);
			this.setExtendedState(MAXIMIZED_BOTH);
			this.setVisible(true);
		}

		private void redrawImage() {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					BufferedImage img = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_ARGB);
					Graphics g = img.getGraphics();
					g.drawImage(capturedImage, 0, 0, null);
					g.setColor(Color.RED);
					g.drawLine(startX, startY, endX, startY);
					g.drawLine(startX, endY, endX, endY);
					g.drawLine(startX, startY, startX, endY);
					g.drawLine(endX, startY, endX, endY);
					imageLabel.setIcon(new ImageIcon(img));
				}
			});
		}

		private void confirmImage() {
			if (endX < startX) {
				int temp = startX;
				startX = endX;
				endX = temp;
			}
			if (endY < startY) {
				int temp = startY;
				startY = endY;
				endY = temp;
			}
			int width = endX - startX + 1;
			int height = endY - startY + 1;
			BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			img.getGraphics().drawImage(capturedImage, 0, 0, width, height, startX, startY, endX + 1, endY + 1, null);
			this.dispose();
			callback.onCaptured(img);
		}

		private void cancel() {
			this.dispose();
			callback.onCancel();
		}

		private class SCMouseListener extends MouseAdapter {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					cancel();
				} else {
					if (startX < 0) {
						startX = e.getX();
						startY = e.getY();
					} else {
						endX = e.getX();
						endY = e.getY();
						confirmImage();
					}
				}
			}
		}

		private class SCMouseMotionListener extends MouseMotionAdapter {
			@Override
			public void mouseMoved(MouseEvent e) {
				if (startX < 0) {
					return;
				}
				endX = e.getX();
				endY = e.getY();
				redrawImage();
			}
		}

	}
}
