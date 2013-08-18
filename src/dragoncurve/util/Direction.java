/**
 * Compass.java
 *
 * Copyright (C) 2012 Anderson de Oliveira Antunes <anderson.utf@gmail.com> ***
 *
 * This file is part of TrafficSimulator.
 *
 * TrafficSimulator is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * TrafficSimulator is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * TrafficSimulator. If not, see http://www.gnu.org/licenses/.
 */

package dragoncurve.util;

/**
 * Classe que define uma forma de apontar para determinados sentidos.
 */
public enum Direction {

     /**
     *         N
     * 
     *    NW   |   NE 
     *       \ | /
     * W  -----o-----  E
     *       / | \
     *    SW   |   SE
     * 
     *         S
     * 
     * 
     */
    
    N(0,-1,0), NE(1,-1,1), E(1,0,2), SE(1,1,3), S(0,1,4), SW(-1,1,5), W(-1,0,6), NW(-1,-1,7);
    
    public final int x;
    public final int y;
    public final int index;
    
    private Direction(int x, int y, int index) {
        this.x = x;
        this.y = y;
        this.index = index;
    }

    public static Direction getDirection (int x, int y) {
        for (Direction c : Direction.values()) {
            if (c.x == x && c.y == y) {
                return c;
            }
        }
        return null;
    }

    public boolean isSameAxis(Direction c) {
        return (x == -c.x && y == -c.y);
    }

    public Direction getOposite() {
        return getDirection(-x,-y);
    }
    
    public Direction[] getAxis() {
        return new Direction[]{this, this.getOposite()};
    }
    
    public Direction[] getOpositeAxis() {
        Direction[] values = Direction.values();
        Direction next2 = values[(index + 2)%8];
        return new Direction[]{next2, next2.getOposite()};
    }
    
    public Direction getPrev (){
        return Direction.values()[(index - 1 < 0)? 7 : index - 1];
    }
    
    public Direction getNext (){
        return Direction.values()[(index + 1 > 7)? 0 : index + 1];
    }

    public int getIndex() {
        return index;
    }

}
