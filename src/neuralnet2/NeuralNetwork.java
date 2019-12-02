package neuralnet2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

class NeuralNetwork implements Serializable {                               // as general a description of a neural network as possible so that it can be used in any NN scenario

    // a subclass defining a neuron
    static class Neuron implements Serializable {
        private int numInputs;                             // each neuron takes in inputs
        ArrayList<Double> weights;                         // whose significance is modified by a weight

        Neuron(int inputs) {
            numInputs = inputs + 1;                        // one extra for the threshold value
            weights = new ArrayList<>(numInputs);
            Random rnd = new Random();
            for (int i = 0; i < numInputs; i++) {          // randomized weight initialization from -1 to 1
                weights.add(rnd.nextDouble() * 2.0 - 1);
            }
        }
    }

    // a subclass defining a layer of neurons in a network
    static class NeuronLayer implements Serializable {
        private int numNeurons;                             // a layer consists of at least one neuron
        ArrayList<Neuron> neurons;                          // the neurons of the layer

        NeuronLayer(int neuronCount, int inputsPerNeuron) {
            numNeurons = neuronCount;
            neurons = new ArrayList<>(numNeurons);
            for (int i = 0; i < neuronCount; i++) {         // randomized neuron initialization
                neurons.add(new Neuron(inputsPerNeuron));
            }
        }
    }

    private int numInputs;                      // a neural net takes in a set of inputs
    private int numOutputs;                     // and delivers a set of outputs
    private int numHiddenLayers;                // between these inputs and outputs are 'hidden' layers of neurons
    private int numNeuronsPerHiddenLayer;       // which may have many neurons to create the many synaptic connections
    private ArrayList<NeuronLayer> layers;

    // initialization/creation of a network given the parameters defining the size of the network
    NeuralNetwork(int numIn, int numOut, int numHidden, int numNeuronPerHidden) {
        numInputs = numIn;
        numOutputs = numOut;
        numHiddenLayers = numHidden;
        numNeuronsPerHiddenLayer = numNeuronPerHidden;
        layers = new ArrayList<>();
        createNet();
    }

    private void createNet() {
        if (numHiddenLayers > 0) {
            // new layer connecting the inputs to the first hidden network if one exists
            layers.add(new NeuronLayer(numNeuronsPerHiddenLayer, numInputs));
            for (int i = 0; i < numHiddenLayers - 1; i++) {
                // for the hidden middle layers, one hidden layer to the next
                layers.add(new NeuronLayer(numNeuronsPerHiddenLayer, numNeuronsPerHiddenLayer));
            }
        }
        // one last layer to connect the last hidden layer to the outputs
        // if there's no hidden layers, just one layer with inputs and outputs
        layers.add(new NeuronLayer(numOutputs, numInputs));
    }

    int getNumberOfWeights() { // returns total number of weights in the whole network
        int numWeights = 0;
        for (NeuronLayer l : layers) {
            for (int j = 0; j < l.numNeurons; j++) {
                numWeights += l.neurons.get(j).weights.size();
            }
        }
        return numWeights;
    }

    void replaceWeights(ArrayList<Double> newWeights) { // ...replaces weights given an input ArrayList
        if (newWeights.size() != getNumberOfWeights()) {
            System.err.println("!! newWeights size does not equal total number of weights !!");
        }
        int cWeight = 0; // index to walk through newWeights
        for (NeuronLayer l : layers) {
            for (Neuron n : l.neurons) {
                for (int k = 0; k < n.numInputs; k++) {
                    n.weights.set(k, newWeights.get(cWeight));
                    cWeight++;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    ArrayList<Double> Update(ArrayList<Double> inputs) { // takes the inputs and computes the outputs having run through the neural net layer
        ArrayList<Double> outputs = new ArrayList<>(numOutputs);
        double netInput;

        if (inputs.size() != numInputs) {
            System.err.println("!! input size does not equal number of inputs !!");
            return outputs;        // empty outputs if incorrect number of inputs
        }

        for (int i = 0; i < numHiddenLayers; i++) { // for each layer
            if (i > 0) {
                inputs = (ArrayList<Double>) outputs.clone(); // make the new inputs be the outputs from the previous iteration of the loop
            }
            outputs.clear();

            for (Neuron n : layers.get(i).neurons) { // for each neuron in each layer
                netInput = 0;
                for (int k = 0; k < inputs.size(); k++) {
                    netInput += n.weights.get(k) * inputs.get(k);
                }
                netInput += n.weights.get(n.weights.size() - 1) + Params.BIAS;
                outputs.add(sigmoid(netInput)); // scale the activation using a sigmoid function
            }
        }
        return outputs;
    }

    private double sigmoid(double activation) { // the sigmoid function returns a value between 0 and 1, <0.5 for negative inputs, >0.5 for positive inputs
        return 1.0 / (1.0 + Math.exp(-activation / Params.ACT_RESPONSE));
    }
}