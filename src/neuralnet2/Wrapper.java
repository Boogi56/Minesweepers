package neuralnet2;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("serial")
public class Wrapper extends JFrame {

    private Wrapper() {
        setSize(Params.WIN_WIDTH + Params.WIN_HRZSPACE, Params.WIN_HEIGHT + Params.WIN_BTNSPACE);
        add(new ControllerMS(Params.WIN_WIDTH, Params.WIN_HEIGHT));
        setResizable(false);
        setTitle("Minesweepers");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            Wrapper go = new Wrapper();
            go.setVisible(true);
        });
    }
}
