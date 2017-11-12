import javax.swing.JFrame;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		VisualSimulator_R simulator = new VisualSimulator_R();
		simulator.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		simulator.setLocation(200, 50);
		simulator.setVisible(true);
		simulator.setResizable(false);
	}

}
