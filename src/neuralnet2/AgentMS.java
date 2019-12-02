package neuralnet2;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

class AgentMS implements Serializable {
    private NeuralNetwork brain;    // each agent has a brain (neural net)
    private Point2D position;       // where the agent is on the map
    private Point2D facing;         // which way they're facing (used as inputs) as an (x, y) pair
    private Point2D cGoodMine;
    private Point2D cBadMine;
    private double rotation;        // the angle from which facing is calculated
    private double speed;           // the speed of the agent
    private double lTrack, rTrack;  // the influence rating toward turning left and turning right, used as outputs
    private double fitness;         // how well the agent is doing, quantified (for the genetic algorithm)
    private double scale;           // the size of the agent

    AgentMS() { // initialization
        Random rnd = new Random();
        brain = new NeuralNetwork(Params.INPUTS, Params.OUTPUTS, Params.HIDDEN, Params.NEURONS_PER_HIDDEN);
        rotation = rnd.nextDouble() * Math.PI * 2;
        lTrack = 0.16;
        rTrack = 0.16;
        fitness = 0;
        scale = Params.SCALE;
        position = new Point2D.Double(rnd.nextDouble() * Params.WIN_WIDTH, rnd.nextDouble() * Params.WIN_HEIGHT);
        facing = new Point2D.Double(-Math.sin(rotation), Math.cos(rotation)); // java starts measuring angles at the 90 degree mark.
    }

    boolean update(ArrayList<Point2D> goodMines, ArrayList<Point2D> badMines) {        // updates all the parameters of the sweeper, sounds fairly important
        ArrayList<Double> inputs = new ArrayList<>();
        // find the closest mine, figure out the direction the mine is from the sweeper's perspective by creating a unit vector
        // your code goes here
        cGoodMine = goodMines.get(getClosestMine(goodMines));
        double xComponent = position.getX() - cGoodMine.getX();
        double yComponent = position.getY() - cGoodMine.getY();
        double divisor = Math.sqrt(Math.pow(xComponent, 2) + Math.pow(yComponent, 2));
        Point2D directionToGoodMine = new Point2D.Double(xComponent / divisor, yComponent / divisor);

        cBadMine = badMines.get(getClosestMine(badMines));
        xComponent = position.getX() - cBadMine.getX();
        yComponent = position.getY() - cBadMine.getY();
        divisor = Math.sqrt(Math.pow(xComponent, 2) + Math.pow(yComponent, 2));
        Point2D directionToBadMine = new Point2D.Double(xComponent / divisor, yComponent / divisor);

        // create the inputs for the neural net
        // your code goes here
        inputs.add(facing.getX());
        inputs.add(facing.getY());
        inputs.add(directionToGoodMine.getX());
        inputs.add(directionToGoodMine.getY());
        inputs.add(directionToBadMine.getX());
        inputs.add(directionToBadMine.getY());

        // get outputs from the sweeper's brain
        ArrayList<Double> output = brain.Update(inputs);
        if (output.size() < Params.OUTPUTS) {
            System.err.println("!! incorrect number of outputs !!");
            return false; // something went really wrong if this happens
        }

        // turn left or turn right?
        lTrack = output.get(0);
        rTrack = output.get(1);
        double rotationForce = lTrack - rTrack;
        rotationForce = Math.min(Params.MAX_TURN_RATE, Math.max(rotationForce, -Params.MAX_TURN_RATE)); // clamp between lower and upper bounds
        rotation += rotationForce;

        // update the speed and direction of the sweeper
//        speed = Params.MAX_SPEED;
        speed = Math.min(Params.MAX_SPEED, lTrack + rTrack);
        facing.setLocation(-Math.sin(rotation), Math.cos(rotation));

        // then update the position, torus style
        double xPos = (Params.WIN_WIDTH + position.getX() + facing.getX() * speed) % Params.WIN_WIDTH;
        double yPos = (Params.WIN_HEIGHT + position.getY() + facing.getY() * speed) % Params.WIN_HEIGHT;
        position.setLocation(xPos, yPos);
        return true;
    }

    int getClosestMine(ArrayList<Point2D> mines) { // finds the mine closest to the sweeper
        double closestSoFar = Point2D.distanceSq(mines.get(0).getX(), mines.get(0).getY(), position.getX(), position.getY());
        int closestMine = 0;
        double lengthOne;
        for (int i = 1; i < mines.size(); i++) {
            lengthOne = Point2D.distanceSq(mines.get(i).getX(), mines.get(i).getY(), position.getX(), position.getY());
            if (lengthOne < closestSoFar) {
                closestSoFar = lengthOne;
                closestMine = i;
            }
        }
        return closestMine;
    }

    int checkForMine(ArrayList<Point2D> mines, int closestMine) { // has the sweeper actually swept up the closest mine to it this tick?
        if (Point2D.distance(position.getX(), position.getY(), mines.get(closestMine).getX(), mines.get(closestMine).getY()) < (Params.MINE_SIZE + scale / 2)) {
            return closestMine;
        }
        return -1;
    }

    void reset() {    // reinitialize this sweeper's position/direction values
        Random rnd = new Random();
        rotation = rnd.nextDouble() * Math.PI * 2;
        position = new Point2D.Double(rnd.nextDouble() * Params.WIN_WIDTH, rnd.nextDouble() * Params.WIN_HEIGHT);
        facing = new Point2D.Double(-Math.sin(rotation), Math.cos(rotation));
        fitness = 0;
    }

    void draw(Graphics2D g) {    // draw the sweeper in its correct place
        AffineTransform at = g.getTransform(); // affine transforms are a neat application of matrix algebra
        // draw the sweeper using a fancy color scheme
        g.rotate(rotation, position.getX(), position.getY()); // they allow you to rotate a g.draw kind of function's output
        g.setColor(new Color(255, 200, 0));

        g.drawOval((int) (position.getX() - scale / 2), (int) (position.getY() - scale / 2), (int) scale, (int) scale);
        if (fitness > 0) {
            g.setColor(new Color(0, Math.min(255, 15 + (int) fitness * 12), Math.min(255, 15 + (int) fitness * 12)));
        } else {
            g.setColor(new Color(Math.min(255, 15 + (int) -fitness * 12), 0, 0));
        }
        g.fillOval((int) (position.getX() - scale / 2) + 1, (int) (position.getY() - scale / 2) + 1, (int) scale - 2, (int) scale - 2);

        g.rotate(0);
        // draw the direction it's facing
        g.setTransform(at); // set the transform back to the normal transform
        g.setColor(new Color(255, 200, 0));
        g.drawLine((int) (position.getX()), (int) (position.getY()), (int) (position.getX() - scale / 2 + facing.getX() * scale), (int) (position.getY() - scale / 2 + facing.getY() * scale));

        g.setColor(new Color(0, 123, 167));
        // drawing lines to mines
        g.drawLine((int) (position.getX()), (int) (position.getY()), (int) (cGoodMine.getX()), (int) (cGoodMine.getY()));
        g.setColor(Color.RED);
        g.drawLine((int) (position.getX()), (int) (position.getY()), (int) (cBadMine.getX()), (int) (cBadMine.getY()));


        // draw its fitness
        g.setColor(new Color(0, 123, 167));
        g.drawString("" + (int) fitness, (int) position.getX() - (int) (scale / 2), (int) position.getY() + 2 * (int) scale);

    }


    void incrementFitness() {
        fitness++;
    } // this may need to get more elaborate pending what you would want sweepers to learn...

    void deIncrimentFitness() {
        fitness -= 2;
    }

    double getFitness() {
        return fitness;
    }

    int getNumberOfWeights() {
        return brain.getNumberOfWeights();
    }

    void setWeights(ArrayList<Double> w) {
        brain.replaceWeights(w);
    }
}