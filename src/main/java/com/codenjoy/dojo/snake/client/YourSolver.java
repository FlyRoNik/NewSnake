package com.codenjoy.dojo.snake.client;


import com.codenjoy.dojo.client.Direction;
import com.codenjoy.dojo.client.Solver;
import com.codenjoy.dojo.client.WebSocketRunner;
import com.codenjoy.dojo.services.Dice;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.services.RandomDice;
import com.codenjoy.dojo.snake.model.Elements;

/**
 * User: your name
 */
public class YourSolver implements Solver<Board> {

    private static final String USER_NAME = "hang.glider.viru007@gmail.com";

    private Dice dice;
    private Board board;

    private Direction prior_X;
    private Direction prior_Y;
    private Point point_app;
    private Point point_snake;
    private Point point_snake_copy;


    public YourSolver(Dice dice) {
        this.dice = dice;
    }

    public static void main(String[] args) {
        start(USER_NAME, WebSocketRunner.Host.REMOTE);
    }

    @Override
    public String get(Board board) {
        this.board = new Board(board.getField());

        point_app = board.getApples().get(0);
        point_snake = board.getHead();
        point_snake_copy = point_snake.copy();



//        point_snake_copy.move(point_snake_copy.getX() + Direction.DOWN.changeX(0), point_snake_copy.getY() + Direction.DOWN.changeY(0));
//        Direction direction = searchDirect(Direction.DOWN);




        //TODO если 2 варианта пути то стоит ли выбирать лучший??
        Direction direction = searchDirect(Direction.STOP);
        if (direction == Direction.STOP) {
            getExit(board);
            getTest();
        }


        assert direction != null;
        return direction.toString();
    }

    private void getTest() {
        System.out.println("DDD");
    }

    private void getExit(Board board) {
        Board saveBoard = this.board;
        this.board = board;



    }

    private Direction searchDirect(Direction direct) {
        if (direct == Direction.STOP) {
            direct = searchLook(board.getField()[point_snake.getX()][point_snake.getY()]).inverted(); //при первом вхождении
        }else {
            setCharInDirect(direct, getLookForDirect(direct));
            point_snake.move(point_snake.getX() + direct.changeX(0), point_snake.getY() + direct.changeY(0));
            direct = direct.inverted(); // для исключения направления откуда пришда змейка
        }

        outMass();

        searchPrior();

        Direction[] arr_direct = {direct.clockwise(), direct.clockwise().clockwise(),
                direct.clockwise().clockwise().clockwise()}; //заполняем массив напрвлениями кроме того откуда пришла

        Direction[] arr_PriorDirect = new Direction[0];
        Direction[] arr_NotPriorDirect = new Direction[0];

        for (Direction anArr_direct : arr_direct) {                         //ищем приоритетные направления
            if (anArr_direct.equals(prior_X) || anArr_direct.equals(prior_Y)) {
                arr_PriorDirect = addToArray(arr_PriorDirect, anArr_direct);
            } else {
                arr_NotPriorDirect = addToArray(arr_NotPriorDirect, anArr_direct);
            }
        }

        getProcessDirect(arr_PriorDirect);     //переставляет напр. совпадающие с напр. змейки вперет
        getProcessDirect(arr_NotPriorDirect);

        if (!point_snake.itsMe(point_app.copy())) {

            for (Direction arr : arr_PriorDirect) {        //проверяем возможность идти по приоритетным направлениям

                char ch = getCharInDirect(arr);

                if (ch == '♣') {
                    return null;
                }

                Direction direction = getDirection(arr_direct, arr, ch);
                if (direction != null) {return direction;}
            }

            for (Direction arr : arr_NotPriorDirect) {     //проверяем возможность идти по не приоритетным направлениям

                char ch = getCharInDirect(arr);

                if (ch == '♣') {
                    return null;
                }

                Direction direction = getDirection(arr_direct, arr, ch);
                if (direction != null) return direction;
            }

        }else {
            return Direction.ACT;
        }

        return Direction.STOP;
    }

    private Direction getDirection(Direction[] arr_direct, Direction arr, char ch) {
        Direction direct;
        if (ch == Elements.NONE.ch() || ch == Elements.GOOD_APPLE.ch()) {     //можно ли двигаться в этом направлении

            boolean back = setAnchor(arr_direct, arr);   //установка ♣, вернет true если есть хоть одно направление
            outMass();
            direct = searchDirect(arr);

            if (direct == Direction.ACT || direct == Direction.STOP || direct == null){
                moveBack(arr);
            }

            if (direct == Direction.ACT) {
                if (point_snake.itsMe(point_snake_copy)) {
                    return arr;
                }
                return Direction.ACT;
            }

            if (direct == Direction.STOP) {
                setCharInDirectQ(arr, '☼');
            }

            if (direct == null) {
                wipeoffLook(arr);
            }

            if (direct == null || direct == Direction.STOP) {
                if (!back) {
                    return Direction.STOP;
                }
                wipeoffAnchor(arr_direct, arr); //стереть ♣
            }
        }
        return null;
    }

    private void moveBack(Direction direction) {
        point_snake.move(point_snake.getX() + direction.inverted().changeX(0), point_snake.getY() + direction.inverted().changeY(0));
    }

    private void getProcessDirect(Direction[] arr) {
        if (arr.length < 3) {
            for (int i = 1; i < arr.length; i++) {
                if (arr[i] == searchLook(getCharInDirect(Direction.STOP))) {
                    Direction direct = arr[i];
                    arr[i] = arr[0];
                    arr[0] = direct;
                }
            }
        }else {
            for (int i = 0; i < arr.length - 1; i++) {
                if (arr[i] == searchLook(getCharInDirect(Direction.STOP))) {
                    Direction direct = arr[i];
                    arr[i] = arr[2];
                    arr[2] = direct;
                }
            }
        }
    }

    private void outMass(){
        for (int i = 0; i < board.getField().length; i++) {
            for (int j = 0; j < board.getField().length; j++) {
                System.out.print(board.getField()[j][i]);
            }
            System.out.println("");
        }
    }

    private void outMass(Board board){
        for (int i = 0; i < board.getField().length; i++) {
            for (int j = 0; j < board.getField().length; j++) {
                System.out.print(board.getField()[j][i]);
            }
            System.out.println("");
        }
    }

    private boolean setAnchor(Direction []arr_direct, Direction direct) {
        boolean flag = false;
        for (Direction d : arr_direct) {
            if (!d.equals(direct)) {
                if (setCharInDirect(d, '♣')) {
                    flag = true;
                }
            }
        }
        return flag;
    }

        private void wipeoffAnchor(Direction []arr_direct, Direction direct) {
        for (Direction d : arr_direct) {
            if (!d.equals(direct)) {
                setCharInDirectA(d, ' ');
            }
        }
    }

    private void wipeoffLook(Direction direct) {
        board.set(point_snake.getX() + direct.changeX(0), point_snake.getY() + direct.changeY(0), ' ');
    }

    private void searchPrior() {
        prior_X = Direction.STOP;
        prior_Y = Direction.STOP;

        int dx = point_snake.getX() - point_app.getX();
        int dy = point_snake.getY() - point_app.getY();

        if (dx < 0) {
            prior_X = Direction.RIGHT;
        }
        if (dx > 0) {
            prior_X = Direction.LEFT;
        }
        if (dy < 0) {
            prior_Y = Direction.DOWN;
        }
        if (dy > 0) {
            prior_Y = Direction.UP;
        }
    }

    private Direction searchLook(char c) {
        switch (c) {
            case '►':{
                return Direction.RIGHT;
            }
            case '◄':{
                return Direction.LEFT;
            }
            case '▲':{
                return Direction.UP;
            }
            case '▼':{
                return Direction.DOWN;
            }
            default: return Direction.STOP;
        }
    }

    private char getLookForDirect(Direction direct) {
        if (Direction.RIGHT == direct) {
            return '►';
        }
        if (Direction.LEFT == direct) {
            return '◄';
        }
        if (Direction.DOWN == direct) {
            return '▼';
        }
        if (Direction.UP == direct) {
            return '▲';
        }
        return ' ';
    }

    private char getCharInDirect(Direction direct) {
        return board.getField()[point_snake.getX() +
                direct.changeX(0)][point_snake.getY() + direct.changeY(0)];
    }

    private Direction[] addToArray(Direction[] array, Direction s) {
        Direction[] ans = new Direction[array.length+1];
        System.arraycopy(array, 0, ans, 0, array.length);
        ans[ans.length - 1] = s;
        return ans;
    }

    private boolean setCharInDirect(Direction direct, char symbol) {
        char ch = getCharInDirect(direct);
        if (ch == Elements.NONE.ch()) {
            board.set(point_snake.getX() + direct.changeX(0), point_snake.getY() + direct.changeY(0), symbol);
            return true;
        }
        return false;
    }

    private boolean setCharInDirectQ(Direction direct, char symbol) {
        char ch = getCharInDirect(direct);
        if (ch == Elements.NONE.ch() || ch == '►' || ch == '◄' || ch == '▲' || ch == '▼') {
            board.set(point_snake.getX() + direct.changeX(0), point_snake.getY() + direct.changeY(0), symbol);
            return true;
        }
        return false;
    }

    private boolean setCharInDirectA(Direction direct, char symbol) {
        char ch = getCharInDirect(direct);
        if (ch == Elements.NONE.ch() || ch == '♣') {
            board.set(point_snake.getX() + direct.changeX(0), point_snake.getY() + direct.changeY(0), symbol);
            return true;
        }
        return false;
    }

    public Direction[] getDirectionSnake(Point point) {
        Direction[] directions = new Direction[0];
        Direction[] pointDirection = board.getAt(point.getX(),point.getY()).getDirectionElement();
        for (Direction d : pointDirection) {
            for (Direction p : board.getAt(d.changeX(0), d.changeY(0)).getDirectionElement()) {
                if (d == p.inverted()) {
                    addToArray(directions,d);
                }
            }
        }
        return directions;
    }

    public static void start(String name, WebSocketRunner.Host server) {
        try {
            WebSocketRunner.run(server, name,
                    new YourSolver(new RandomDice()),
                    new Board());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
