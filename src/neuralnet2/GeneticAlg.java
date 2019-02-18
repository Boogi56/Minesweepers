package neuralnet2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GeneticAlg implements Serializable{

    class Genome implements Comparable<Genome>, Serializable {      // a simple class to handle a single genome
        private ArrayList<Double> weights;            // since no other class needs to know how Genome is implemented
        private double fitness;                       // it is a subclass of the genetic algorithm class

        Genome() {
            weights = new ArrayList<>();              // here the 'chromosomes' for a genetic alg influenced by a neural net are the weights of the neuron's inputs
            fitness = 0;                              // fitness increases as the genome becomes more fit
        }

        Genome(ArrayList<Double> w, double f) {
            weights = new ArrayList<>();
            for (Double d : w) {
                weights.add(d);
            }
            fitness = f;
        }

        public Genome clone() {                       // mmm, cloning genomes
            return new Genome(weights, fitness);
        }

        ArrayList<Double> getWeights() {
            return weights;
        }

        double getFitness() {
            return fitness;
        }

        void setFitness(double f) {
            fitness = f;
        }

        @Override
        public int compareTo(Genome o) {                    // the comparable interface needs a definition of compareTo
            if (this.fitness > o.getFitness()) {            // the interface is being used so that the genomes can be sorted by fitness
                return 1;
            } else if (this.fitness < o.getFitness()) {
                return -1;
            }
            return 0;
        }
    }

    private ArrayList<Genome> pop;      // the genomes (weights for neural nets) who are the members of the genetic algorithm's gene pool
    private int popSize;                // the pools' size
    private int chromosomeLength;       // the length of the weights list
    private double totalFitness;        // the summation of all the genomes' fitnesses
    private double bestFitness;         // the best fitness of all the genomes, then the average, then the worst
    private double avgFitness;          // could be used for plotting fitnesses
    private double worstFitness;
    private int fittestGenome;          // the index of the most fit genome in the population
    private int genCount;               // what generation the pool has made it to
    private double mutationRate;        // how often mutation (for each entry in a weight list) and crossover occurs
    private double crossoverRate;
    private ArrayList<Double> child1;
    private ArrayList<Double> child2;

    GeneticAlg(int populationSize, double mutRate, double crossRate, int numWeights) {
        popSize = populationSize;
        mutationRate = mutRate;
        crossoverRate = crossRate;
        chromosomeLength = numWeights;
        totalFitness = 0;
        genCount = 0;
        fittestGenome = 0;
        bestFitness = 0;
        worstFitness = 99999999;
        avgFitness = 0;
        // initialize population with randomly generated weights
        pop = new ArrayList<>();
        Random rnd = new Random();
        for (int i = 0; i < popSize; i++) {
            pop.add(new Genome());
            for (int j = 0; j < chromosomeLength; j++) {
                pop.get(i).weights.add(rnd.nextDouble() * 2 - 1);
            }
        }
    }

    private int randInt(int min, int max) {
        return min + (int) (Math.random() * max);
    }

    @SuppressWarnings("unchecked")
    public void crossover(ArrayList<Double> parent1, ArrayList<Double> parent2) {
        // implement crossover, similar to the previous project

        if (Math.random() > Params.CROSSOVER_RATE) {
            int crossoverIndex = randInt(1, parent1.size() - 1);
            int parentSize = parent1.size();
            for(int i = crossoverIndex; i < parentSize; i++){
                parent1.add(parent2.get(i));
                parent2.add(parent1.get(i));
            }
            parent1.subList(parentSize, parentSize + (parentSize - crossoverIndex)).clear(); // remove the crossed over section that was added to other parent
            parent2.subList(parentSize, parentSize + (parentSize - crossoverIndex)).clear();
        }
    }

    public void mutate(ArrayList<Double> chromo) {
        // mutate each weight dependent upon the mutation rate
        // the weights are bounded by the maximum allowed perturbation

        if (Math.random() > Params.MUTATION_RATE) {
            for(int i = 0; i < chromo.size(); i++){
                chromo.set(i, chromo.get(i) + (Math.random() * Params.MAX_PERTURBATION - (Params.MAX_PERTURBATION / 2)));
            }
        }
    }

    public Genome getChromoByRoulette() {        // random parent selection using a roulette approach
        Random rnd = new Random();
        double stop = rnd.nextDouble() * totalFitness;    // pick a random fitness value at which to stop
        double fitnessSoFar = 0;
        int i = 0;
        do {
            fitnessSoFar += pop.get(i).fitness;
            i++;
        } while (fitnessSoFar < stop);
        return pop.get(i-1).clone();
    }

    @SuppressWarnings("unchecked")
    ArrayList<Genome> epoch(ArrayList<Genome> oldpop) {        // get the new generation from the old generation
        pop = (ArrayList<Genome>) oldpop.clone();              // the previous population is the current population
        reset();                                               // reinitialize fitness stats
        Collections.sort(pop);                                 // sort them by fitness
        calculateBestWorstAvgTot();                            // calculate the fitness stats
        ArrayList<Genome> newPop = new ArrayList<>();
        int parentOneIndex, parentTwoIndex;
        if (Params.NUM_COPIES_ELITE * Params.NUM_ELITE % 2 == 0) {               // take the top NUM_ELITE performers and add them to the new population
            grabNBest(Params.NUM_ELITE, Params.NUM_COPIES_ELITE, newPop);
        }
        while (newPop.size() < popSize) {                      // fill the rest of the new population by children from parents using the classic genetic algorithm
            Genome parentOne = getChromoByRoulette();
            Genome parentTwo = getChromoByRoulette();
            crossover(parentOne.getWeights(), parentTwo.getWeights());
            mutate(parentOne.getWeights());
            mutate(parentTwo.getWeights());
            newPop.add(parentOne);
            if(newPop.size() < popSize){
                newPop.add(parentTwo);
            }
        }
        pop = (ArrayList<Genome>) newPop.clone();
        return pop;                                            // this probably could have been written better, why return a class variable?
    }

    private void grabNBest(int nBest, int numCopies, ArrayList<Genome> popList) { // hopefully the population is sorted correctly...
        while (nBest-- > 0) {
            for (int i = 0; i < numCopies; i++) {
                popList.add(pop.get(popSize - 1 - nBest));
            }
        }
    }

    private void calculateBestWorstAvgTot() { // fairly self-explanatory, try commenting it
        totalFitness = 0;
        bestFitness = pop.get(0).getFitness();
        worstFitness = pop.get(0).getFitness();
        fittestGenome = 0;
        for (int i = 0; i < popSize; i++) {
            if (pop.get(i).fitness > bestFitness) {
                bestFitness = pop.get(i).fitness;
                fittestGenome = i;
            }
            else if (pop.get(i).fitness < worstFitness) {
                worstFitness = pop.get(i).fitness;
            }
            totalFitness += pop.get(i).fitness;
        }
        avgFitness = totalFitness / popSize;
    }

    private void reset() {        // reset fitness stats
        totalFitness = 0;
        bestFitness = 0;
        worstFitness = Double.MAX_VALUE;
        avgFitness = 0;
    }

    // self-explanatory
    ArrayList<Genome> getChromosomes() {
        return pop;
    }

    double avgFitness() {
        return totalFitness / popSize;
    }

    double bestFitness() {
        return bestFitness;
    }

    void setPop(ArrayList<Genome> pop) {
        this.pop = pop;
    }


}