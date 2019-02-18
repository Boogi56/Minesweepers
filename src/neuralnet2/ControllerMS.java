package neuralnet2;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.*;

import neuralnet2.GeneticAlg.Genome;

@SuppressWarnings("serial")
public class ControllerMS extends JPanel implements ActionListener {

    private int ticks;                       // tick counter for a run of a generation's agents
    private int generations;                 // the counter for which generation the sim's on
    private int numAgents;                   // how many agents
    private GeneticAlg GA;                   // the genetic algorithm that manages the genome weights
    private ArrayList<Genome> pop;           // the weights of the neural nets for each of the agents
    private ArrayList<AgentMS> agents;       // the agents themselves (the sweepers)
    private ArrayList<Point2D> goodMines;    // the goodMines
    private ArrayList<Point2D> badMines;     // the badMines
    private ArrayList<Double> avgFitness;    // useful if you were plotting the progression of fitness
    private ArrayList<Double> bestFitness;
    private BufferedImage pic;               // the image in which things are drawn
    private Image asteroid;
    private JLabel picLabel;                 // the label that holds the image
    private JLabel dataLabel;                // the label that holds the fitness information
    private JButton saveBtn;
    private JButton loadBtn;
    private DecimalFormat df;

    ControllerMS(int xDim, int yDim) {
        setBackground(Color.LIGHT_GRAY);
        // addMouseListener(new MAdapter());
        // addMouseMotionListener(new MAdapter());
        setFocusable(true);
        setDoubleBuffered(true);
        // create the things to display, then add them
        pic = new BufferedImage(xDim, yDim, BufferedImage.TYPE_INT_RGB);
        picLabel = new JLabel(new ImageIcon(pic));
        dataLabel = new JLabel("                                                                                                        ", SwingConstants.CENTER);
        dataLabel.setFont(new Font("TimesNewRomanPSMT", Font.BOLD, 20));
        try {
            asteroid = ImageIO.read(new File("resources/asteroid.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }


        // initialize all of the variables!
        numAgents = Params.SWEEPERS;
        ticks = 0;
        generations = 0;
        avgFitness = new ArrayList<>();
        bestFitness = new ArrayList<>();
        df = new DecimalFormat("#0.00");

        // make up agents
        agents = new ArrayList<>(numAgents);
        for (int i = 0; i < numAgents; i++) {
            agents.add(new AgentMS());
        }

        // give agent neural nets their weights
        GA = new GeneticAlg(numAgents, Params.MUTATION_RATE, Params.CROSSOVER_RATE, agents.get(0).getNumberOfWeights());
        pop = GA.getChromosomes();
        for (int i = 0; i < numAgents; i++) {
            agents.get(i).setWeights(pop.get(i).getWeights());
        }

        // set up the goodMines
        goodMines = new ArrayList<>(Params.GOODMINES);
        badMines = new ArrayList<>(Params.BADMINES);
        Random rnd = new Random();
        for(int i = 0; i < Params.MINES; i++){
            if(i < Params.GOODMINES){
                goodMines.add(new Point2D.Double(rnd.nextDouble() * xDim, rnd.nextDouble() * yDim));
            }
            if(i >= Params.GOODMINES){
                badMines.add(new Point2D.Double(rnd.nextDouble() * xDim, rnd.nextDouble() * yDim));

            }
        }

        initBtn();
        addThingsToPanel();

        // start it up!
        // timer runs the simulation
        Timer timer = new Timer(1, this);
        timer.start();
    }

    private void drawThings(Graphics2D g) {
        // cover everything with a blank screen
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, pic.getWidth(), pic.getHeight());
        // draw agents
        for (AgentMS a : agents) {
            a.draw(g);
        }
        // draw mines
        g.setColor(new Color(0,123,167));
        for (Point2D m : goodMines) {
            g.fillOval((int) (m.getX() - Params.MINE_SIZE / 2), (int) (m.getY() - Params.MINE_SIZE / 2), (int) Params.MINE_SIZE, (int) Params.MINE_SIZE);
        }

        g.setColor(Color.RED);
        for (Point2D m : badMines){
                g.drawImage(asteroid, (int)m.getX() - 9, (int)m.getY() - 9, null);
//            g.fillRect((int) (m.getX() - Params.MINE_SIZE / 2), (int) (m.getY() - Params.MINE_SIZE / 2), (int) Params.MINE_SIZE, (int) Params.MINE_SIZE);
        }
    }

    private void initBtn() {
        saveBtn = new JButton("Save");
        saveBtn.addActionListener(e -> {
            try {
                FileOutputStream fileOut = new FileOutputStream(new File("tmp/agents.ser"));
                ObjectOutputStream out = new ObjectOutputStream(fileOut);
                out.writeObject(agents);
                fileOut = new FileOutputStream(new File("tmp/pop.ser"));
                out.writeObject(pop);
                out.close();
                fileOut.close();
                System.out.println("Serialized data is saved in /tmp/agents.ser and /tmp/pop.ser");

                BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
                paint(img.getGraphics());
                ImageIO.write(img, "png", new File("Game State.png"));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        loadBtn = new JButton("Load");
        loadBtn.addActionListener(e -> {
            try {
                FileInputStream fileIn = new FileInputStream("tmp/agents.ser");
                ObjectInputStream in = new ObjectInputStream(fileIn);
                agents = (ArrayList<AgentMS>) in.readObject();
                fileIn = new FileInputStream("tmp/pop.ser");
                pop = (ArrayList<Genome>)in.readObject();
                ticks = 0;
                in.close();
                fileIn.close();
            } catch (IOException | ClassNotFoundException e1){
                e1.printStackTrace();
            }
        });
    }

    private void addThingsToPanel() {
        setLayout(new GridBagLayout()); // GridBagLayout uses a coordinate system to place components
        GridBagConstraints c = new GridBagConstraints(); // a GridBagConstraints object is used to store the placement, size, and other characteristics of a component
        c.weightx = 0.5; // establishes how space is distributed among each element in the x direction
        c.fill = GridBagConstraints.HORIZONTAL; // have each component take up all its space in the horizontal dimension
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 0;
        add(picLabel, c); // (0, 0)
        c.insets = new Insets(0,0,0,150);
        c.gridwidth = 1;
        c.gridy = 1;
        add(saveBtn, c);
        c.gridy = 2;
        add(loadBtn, c);
        c.gridy = 1;
        c.gridx = 1;
        c.gridheight = 2;
        add(dataLabel, c);
    }

    private void updateAgents() {
        ticks++;  // count ticks to set the length of the generation run
        if (ticks < Params.TICKS) { // do another tick toward finishing a generation
            Random rnd = new Random();
            // update each agent by calling their update function and checking to see if they got a mine
            for (int i = 0; i < numAgents; i++) {
                if (!agents.get(i).update(goodMines, badMines)) {
                    System.err.println("!! wrong amount of neural net inputs !!");
                    break;
                }
                // did it find a mine
                int closestMine = agents.get(i).getClosestMine(goodMines);
                int foundMine = agents.get(i).checkForMine(goodMines, closestMine);
                // if it found a mine, add to that agent's fitness and make a new mine
                if (foundMine >= 0) {
                    agents.get(i).incrementFitness();
                    goodMines.set(foundMine, new Point2D.Double(rnd.nextDouble() * pic.getWidth(), rnd.nextDouble() * pic.getHeight()));
                }

                closestMine = agents.get(i).getClosestMine(badMines);
                foundMine = agents.get(i).checkForMine(badMines, closestMine);
                if(foundMine >= 0) {
                    agents.get(i).deIncrimentFitness();
                    badMines.set(foundMine, new Point2D.Double(rnd.nextDouble() * pic.getWidth(), rnd.nextDouble() * pic.getHeight()));
                }

                // keep track of that agent's fitness in the GA as well as the NN
                pop.get(i).setFitness(agents.get(i).getFitness());
            }
        } else { // a generation has completed, run the genetic algorithm and update the agents
//            avgFitness.add(GA.avgFitness());
//            bestFitness.add(GA.bestFitness());
//            System.out.println("avgFitness = " + avgFitness);
//            System.out.println("bestFitness = " + bestFitness);
            generations++;
            ticks = 0;
            pop = GA.epoch(pop); // the big genetic algorithm process line
            dataLabel.setText("Generation " + generations + " has average fitness of " + df.format(GA.avgFitness()) + " and best fitness of " + (int) GA.bestFitness() + ".");
            for (int i = 0; i < numAgents; i++) { // give the agents all the new weights information
                agents.get(i).setWeights(pop.get(i).getWeights());
                agents.get(i).reset();
            }
        }
    }

    // @Override
    public void actionPerformed(ActionEvent e) {
        updateAgents();
        drawThings((Graphics2D) pic.getGraphics());
        repaint();
    }

    private void createUIComponents() {

    }
}