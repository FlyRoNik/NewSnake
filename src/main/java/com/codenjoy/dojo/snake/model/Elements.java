package com.codenjoy.dojo.snake.model;

import com.codenjoy.dojo.client.Direction;
import com.codenjoy.dojo.services.CharElements;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.snake.client.Board;
import com.codenjoy.dojo.snake.client.YourSolver;

/**
 * User: oleksandr.baglai
 * Date: 10/1/12
 * Time: 3:57 AM
 */
public enum Elements implements CharElements {
    BAD_APPLE('☻'),
    GOOD_APPLE('☺'),

    BREAK('☼'),

    HEAD_DOWN('▼'),
    HEAD_LEFT('◄'),
    HEAD_RIGHT('►'),
    HEAD_UP('▲'),

    TAIL_END_DOWN('╙',Direction.UP),
    TAIL_END_LEFT('╘',Direction.RIGHT),
    TAIL_END_UP('╓',Direction.DOWN),
    TAIL_END_RIGHT('╕',Direction.LEFT),
    TAIL_HORIZONTAL('═',Direction.LEFT,Direction.RIGHT),
    TAIL_VERTICAL('║',Direction.UP,Direction.DOWN),
    TAIL_LEFT_DOWN('╗',Direction.LEFT,Direction.DOWN),
    TAIL_LEFT_UP('╝',Direction.UP,Direction.LEFT),
    TAIL_RIGHT_DOWN('╔',Direction.DOWN,Direction.RIGHT),
    TAIL_RIGHT_UP('╚',Direction.UP,Direction.RIGHT),

    NONE(' ');

    final char ch;
    final Direction[] direction;

    Elements(char ch,Direction ... directions) {
        this.ch = ch;
        this.direction = new Direction[directions.length];
        System.arraycopy(directions, 0, this.direction, 0, directions.length);
    }

    @Override
    public String toString() {
        return String.valueOf(ch);
    }

    @Override
    public char ch() {
        return ch;
    }

    public static Elements valueOf(char ch) {
        for (Elements el : Elements.values()) {
            if (el.ch == ch) {
                return el;
            }
        }
        throw new IllegalArgumentException("No such element for " + ch);
    }

    public Direction[] getDirectionElement(){
        Direction[] directions = new Direction[this.direction.length];
        System.arraycopy(this.direction,0,directions,0,this.direction.length);
        return directions;
    }

    private Direction[] remove(Direction[] symbols, int index)
    {
        if (index >= 0 && index < symbols.length)
        {
            Direction[] copy = new Direction[symbols.length-1];
            System.arraycopy(symbols, 0, copy, 0, index);
            System.arraycopy(symbols, index+1, copy, index, symbols.length-index-1);
            return copy;
        }
        return symbols;
    }
}
