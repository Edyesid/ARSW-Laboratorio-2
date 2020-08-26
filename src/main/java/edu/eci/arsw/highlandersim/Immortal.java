package edu.eci.arsw.highlandersim;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class Immortal extends Thread {

    private ImmortalUpdateReportCallback updateCallback=null;
    
    private int health;
    
    private int defaultDamageValue;

    private final CopyOnWriteArrayList<Immortal> immortalsPopulation;

    private final String name;

    private final Random r = new Random(System.currentTimeMillis());
    
    private boolean flag;


    public Immortal(String name, CopyOnWriteArrayList<Immortal> immortalsPopulation, int health, int defaultDamageValue, ImmortalUpdateReportCallback ucb) {
        super(name);
        this.updateCallback=ucb;
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.health = health;
        this.defaultDamageValue=defaultDamageValue;
        this.flag = false;
    }

    public void run() {

        while (true) {
        	
        	Immortal im;
        	
        	if (flag) {
        		synchronized(this) {
        			
        			try {
						wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
        			
        			flag = false;
        		}
        	}
        	
            

            int myIndex = immortalsPopulation.indexOf(this);

            int nextFighterIndex = r.nextInt(immortalsPopulation.size());

            //avoid self-fight
            if (nextFighterIndex == myIndex) {
                nextFighterIndex = ((nextFighterIndex + 1) % immortalsPopulation.size());
            }

            im = immortalsPopulation.get(nextFighterIndex);

            this.fight(im);

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }

    public void fight(Immortal i2) {
    	
    	if (this.getHealth() > 0) {
        	Immortal first;
        	Immortal second;
        	if (this.getId() < i2.getId()) {
        		first = this;
        		second = i2;
        	} else {
        		first = i2;
        		second = this;
        	}
        	synchronized(first) {
        		synchronized(second) {
        			if (i2.getHealth() > 0) {
        	            i2.changeHealth(i2.getHealth() - defaultDamageValue);
        	            this.health += defaultDamageValue;
        	            updateCallback.processReport("Fight: " + this + " vs " + i2+"\n");
        	        } else {
        	        	immortalsPopulation.remove(i2);
        	            updateCallback.processReport(this + " says:" + i2 + " is already dead!\n");
        	        }
        		}
        	}
    	} else {
    		immortalsPopulation.remove(this);
    	}

    	
        

    }

    public void changeHealth(int v) {
        health = v;
    }

    public int getHealth() {
        return health;
    }

    @Override
    public String toString() {

        return name + "[" + health + "]";
    }
    
    public void pause() {
    	flag = true;
    }
    
    public void continuar() {
    	synchronized(this) {
    		notify();
    	}
    }

}
