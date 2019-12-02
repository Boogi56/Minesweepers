package neuralnet2;

class Params {
    // general parameters
    static final int WIN_SIZE = 690;            // width of world map (DEPRECATED)
    static final int WIN_WIDTH = 1035;          // width of world map
    static final int WIN_HEIGHT = 690;          // height of world map
    static final int WIN_BTNSPACE = 85;
    static final int WIN_HRZSPACE = 8;

    // for the neural network
    static final int INPUTS = 6;                // number of inputs
    static final int HIDDEN = 2;    // 1        // number of hidden layers
    static final int NEURONS_PER_HIDDEN = 5;    // number of neurons in each hidden layer
    static final int OUTPUTS = 2;               // number of outputs
    static final double BIAS = -1;              // the threshold (bias) value
    static final double ACT_RESPONSE = 1;       // adjusts the sigmoid function

    // for the genetic algorithm
    static final double CROSSOVER_RATE = 0.3;    // the chance of crossover happening
    static final double MUTATION_RATE = 0.1;     // the chance of a particular value in a genome changing
    static final double MAX_PERTURBATION = 0.3;  // maximum magnitude of the new value from mutation
    static final int NUM_ELITE = 4;              // how many of the top performers advance to the next generation
    static final int NUM_COPIES_ELITE = 1;       // and how many copies of those performers we'll use

    // these are specific to the mine sweeping scenario
    // for the controller to run the whole simulation
    static final int MINES = 100; // 60
    static final int GOODMINES = 17*MINES/20;
    static final int BADMINES = MINES-GOODMINES; // used to be: MINES/4
    static final int SWEEPERS = 45;
    static final int TICKS = 1600;               // how long agents have a chance to gain fitness
    static final double MINE_SIZE = 4;

    // for the mine sweepers
    static final double MAX_TURN_RATE = 0.2;     // how quickly they may turn
    static final double MAX_SPEED = 1.5; // 2    // how fast they can go
    static final int SCALE = 15;                 // the size of the sweepers

}