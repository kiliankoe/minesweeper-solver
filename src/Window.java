import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Window.java
 * Minesweeper Solver
 * Created by Kilian Koeltzsch on 13.06.13.
 * It's all CC-BY-SA 3.0, baby!
 */

public class Window extends JFrame {

	public Window() {
		initUI();
	}

	public final void initUI() {
		JPanel panel = new JPanel();
		getContentPane().add(panel);
		panel.setLayout(null);

		JButton startButton = new JButton("Start");
		startButton.setBounds(20,20,260,110);
		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				//todo: Stuff
			}
		});
		startButton.setToolTipText("click to start solving Minesweeper");

		JButton cancelButton = new JButton("Cancel");
		cancelButton.setBounds(210,140,80,30);
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				//todo: Slightly less Stuff
			}
		});
		cancelButton.setToolTipText("cancel the running algorithms");

		panel.add(startButton);
		panel.add(cancelButton);

		setTitle("Minesweeper Solver");
		setSize(300,200);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Window window = new Window();
				window.setVisible(true);
			}
		});
	}

}
