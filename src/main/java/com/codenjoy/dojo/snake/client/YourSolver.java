package com.codenjoy.dojo.snake.client;


import com.codenjoy.dojo.client.Direction;
import com.codenjoy.dojo.client.Solver;
import com.codenjoy.dojo.client.WebSocketRunner;
import com.codenjoy.dojo.services.Dice;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.services.PointImpl;
import com.codenjoy.dojo.services.RandomDice;
import com.codenjoy.dojo.snake.model.Elements;

/**
 * User: your name
 */
public class YourSolver implements Solver<Board> {

    private static final String USER_NAME = "hang.glider.viru007@gmail.com";

    private Dice dice;
    private Board board;
    private Board boardSave;
    private Board boardSave1;

    private boolean f1 = false;

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

    public static void start(String name, WebSocketRunner.Host server) {
        try {
            WebSocketRunner.run(server, name,
                    new YourSolver(new RandomDice()),
                    new Board());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String get(Board board) {
        this.board = new Board(board.getField()); //TODO Локальная борда
        this.boardSave = new Board(board.getField());
        this.boardSave1 = new Board(board.getField());
        Point pointTeil = this.board.get(Elements.TAIL_END_UP,
                Elements.TAIL_END_DOWN,
                Elements.TAIL_END_LEFT,
                Elements.TAIL_END_RIGHT).get(0);

        this.board.set(pointTeil.getX(),pointTeil.getY(),' ');

        point_app = board.getApples().get(0);
        point_snake = board.getHead();
        point_snake_copy = point_snake.copy();

        if (getCharInDirect(searchLook(this.board.getAt(point_snake).ch()).inverted(),point_snake) ==
                getNeck(this.board.getAt(point_snake).ch())) {
            return Direction.STOP.toString();
        }

        //TODO если 2 варианта пути то стоит ли выбирать лучший??
        Direction direction = searchDirect(Direction.STOP);

        if (direction == Direction.STOP) {

            int count = getExit(new Board(board.getField()),pointTeil);
            this.board = new Board(board.getField());
            this.board.set(point_app.getX(), point_app.getY(), ' ');
            point_app.move(pointTeil);
            this.board.set(point_app.getX(), point_app.getY(), '☺');
            //outMass();
            //outMass(board);
//            direction = searchDirect(Direction.STOP, count - 1);
            direction = searchDirect(Direction.STOP, count);
        }

        if (direction == Direction.STOP || direction == null) {
            direction = searchLook(board.getAt(point_snake.copy()).ch()).inverted();
        }
        return direction.toString();
    }

    private boolean getTailInto(Point point) {
        Direction direct = Direction.UP;

        for (int i = 0; i < 3; i++) {
            Point temp = getPointInDirect(direct, point);
            if (getCharInDirect(direct, point) == '☼' &&
                    temp.getX() > 0 && temp.getY() > 0) {
                return true;
            }
            direct = direct.clockwise();
        }
        return false;
    }

    private char getNeck(char c) {
        if (c == '►' || c == '◄') {
            return '║';
        }
        if (c == '▲' || c == '▼') {
            return '═';
        }
        System.out.println("Fail!!");
        return ' ';
    }

    private int getExit(Board board,Point point) {
        Board board1 = new Board(board.getField());
        this.board = new Board(board.getField());
        int count = 0;
        boolean flag = false;
        Point point1 = null;
        Point pointTail = new PointImpl(point);
        while (true) {
            Direction[] direction = getDirectionSnake(this.board,point);
            //assert direction != null;
            if (direction == null) {
                return -1;
            }
            Direction direct = direction[0]; //TODO ArrayIndexOutOfBoundsException: 0
            if (direction.length < 2) {
                outMass();
                board.set(point.getX(), point.getY(), ' ');
                this.board = new Board(board.getField());
                //outMass(board);
                if (searchDirect(Direction.STOP) != Direction.STOP) {
                    if (point_snake.itsMe(getPointInDirect(searchLook(this.board.getAt(point).ch()).inverted(), point))) {
                        if (board.getAt(getPointInDirect(searchLook(board.getAt(point_snake.copy()).ch()), point_snake)).ch() == ' ') {
                            flag = true;
                            point1 = new PointImpl(point);
                        } else {
                            board.set(point.getX(), point.getY(), '☼');
                        }
                        //outMass(board);
                    } else {
                        if (flag) {
                            point.move(getPointInDirect(searchLook(this.board.getAt(point_snake).ch()), point_snake));
                            board.set(point1.getX(), point1.getY(), '☼');
                        } else {
                            if (board1.getAt(getPointInDirect(searchLook(board1.getAt(point_snake.copy()).ch()), point_snake)).ch() != ' ') {
                                count++;
                            }
                            point.move(getPointInDirect(searchLook(this.board.getAt(point).ch()).inverted(), point));
                        }
                        this.board = new Board(board.getField());
                        return count;
                    }
                }
                //outMass();
                if (count == 0) {
                    f1 = getTailInto(pointTail);
                }
                point.move(point.getX() + direct.changeX(0), point.getY() + direct.changeY(0));
            }
            count++;
        }
    }

    private void getCutSnake() {
        Board board = new Board(boardSave.getField()); //TODO борда не сохраняется
        Point tail = this.boardSave.get(Elements.TAIL_END_UP,
                Elements.TAIL_END_DOWN,
                Elements.TAIL_END_LEFT,
                Elements.TAIL_END_RIGHT).get(0);

        Direction[] direction = getDirectionSnake(board, tail);
        //outMass(board);
        board.set(tail.getX(), tail.getY(), ' ');
        this.board.set(tail.getX(), tail.getY(), ' ');
        //outMass(board);
        if (direction == null) {
            return;
        }
        Direction direct = direction[0];
        tail.move(tail.getX() + direct.changeX(0), tail.getY() + direct.changeY(0));
        direction = getDirectionSnake(board, tail);
        if (direction == null) {
            return;
        }
        direct = direction[0];

        board.set(tail.getX(), tail.getY(), getTailForDirect(direct));
        this.board.set(tail.getX(), tail.getY(), getTailForDirect(direct));
        this.boardSave = new Board(board.getField());
        this.board.set(point_app.getX(), point_app.getY(), '☺');

        //outMass(board);
        //outMass();


//        Point app = board.get(Elements.GOOD_APPLE).get(0);
//        board.set(app.getX(), app.getY(), ' ');
//        point_app.move(point_app);
//        board.set(point_app.getX(), point_app.getY(), '☺');

        //this.board = new Board(board.getField());
    }

    private void getSpliceSnake() {
        Point tail = this.boardSave.get(Elements.TAIL_END_UP,
                Elements.TAIL_END_DOWN,
                Elements.TAIL_END_LEFT,
                Elements.TAIL_END_RIGHT).get(0);
        Elements element = boardSave1.getAt(tail);
        this.board.set(tail.getX(), tail.getY(), element.ch());

        //outMass();

        Direction[] direction = element.getDirectionElement();

        Direction[] direction1 = getDirectionSnake(boardSave, tail);
        if (direction1 == null) {
            return;
        }
        Direction direct = direction1[0];


        for (int i = 0; i < direction.length; i++) {
            if (direction[i] != direct) {
                this.boardSave.set(tail.getX(), tail.getY(), element.ch());
                tail.move(tail.getX() + direction[i].changeX(0), tail.getY() + direction[i].changeY(0));
                this.board.set(tail.getX(), tail.getY(), getTailForDirect(direction[i].inverted()));
                this.boardSave.set(tail.getX(), tail.getY(), getTailForDirect(direction[i].inverted()));
            }
        }


        this.board.set(point_app.getX(), point_app.getY(), '☺');
        //outMass();
    }

    //TODO Слишком долго вычисляет()
    private Direction searchDirect(Direction direct, int count) {
        if (direct == Direction.STOP) {
            direct = searchLook(board.getField()[point_snake.getX()][point_snake.getY()]).inverted(); //при первом вхождении
        }else {
            setCharInDirect(direct, getLookForDirect(direct));
            point_snake.move(point_snake.getX() + direct.changeX(0), point_snake.getY() + direct.changeY(0));
            direct = direct.inverted(); // для исключения направления откуда пришда змейка
            if (f1) {
                getCutSnake(); //TODO Может обрезать всю змейку
            }
        }

        //outMass();

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

        //getProcessDirect(arr_PriorDirect);     //переставляет напр. совпадающие с напр. змейки вперет
        getProcessDirect(arr_NotPriorDirect);

        if (!point_snake.itsMe(point_app.copy())) {

            for (Direction arr : arr_PriorDirect) {        //проверяем возможность идти по приоритетным направлениям

                char ch = getCharInDirect(arr,point_snake);

//                if (ch == '♣') {
//                    return null;
//                }

                //outMass();
                Direction direction = getDirection(arr_direct, arr, ch, count);
                if (direction != null) {return direction;}
            }
            //outMass();

            for (Direction arr : arr_NotPriorDirect) {     //проверяем возможность идти по не приоритетным направлениям
                getProcessDirect(arr_NotPriorDirect);

                if (arr_PriorDirect.length == 1 && arr_PriorDirect[0] == arr.inverted()) {
                    if (!point_snake.itsMe(point_snake_copy)) {
                        break;
                    }
                }

                char ch = getCharInDirect(arr, point_snake);

//                if (ch == '♣') {
//                    return null;
//                }

                //outMass();
                Direction direction = getDirection(arr_direct, arr, ch, count);
                if (direction != null) return direction;
            }

        }else {
            if (count <= 0) {
                //outMass();


                return Direction.ACT;
            }else {
                //outMass();
                return null;
            }
        }

        return Direction.STOP;
    }

    private Direction getDirection(Direction[] arr_direct, Direction arr, char ch, int count) {
        Direction direct;
        if (ch == Elements.NONE.ch() || ch == Elements.GOOD_APPLE.ch()) {     //можно ли двигаться в этом направлении

            boolean back = setAnchorL(arr_direct, arr);   //установка ♣, вернет true если есть хоть одно направление
            count--;
            //outMass();
            direct = searchDirect(arr, count);
            //outMass();

            if (direct == Direction.ACT || direct == Direction.STOP || direct == null){
                moveBack(arr);
                //outMass();
            }

            if (direct == Direction.ACT) {
                if (point_snake.itsMe(point_snake_copy)) {
                    outMass();
                    return arr;
                }
                return Direction.ACT;
            }

            if (direct == Direction.STOP) {
                //setCharInDirectQ(arr, ' ');
                setCharInDirectQ(arr, '♥');
            }

            if (direct == null) {
//                wipeoffLook(arr);
            }

            //outMass();
            if (direct == null || direct == Direction.STOP) {
                wipeoffLook(arr);
                if (f1) {
                    getSpliceSnake();
                }
                count++;
                if (!back) {
                    return Direction.STOP;
                }
                wipeoffAnchor(arr_direct, arr); //стереть ♣
            }
        }
        return null;
    }

    private String longDirect(Point point_snake, Point point_exit) {
        Direction direct = searchLook(getCharInDirect(Direction.STOP, point_snake));
        Point point_nextStep0, point_nextStep1, point_nextStep2;
        Direction[] arr_direct = new Direction[0];

        if(getCharInDirect(direct.inverted().clockwise(), point_snake)==Elements.NONE.ch()){
            arr_direct=addToArray(arr_direct,direct.inverted().clockwise());
        }
        if(getCharInDirect(direct.inverted().clockwise().clockwise(), point_snake)==Elements.NONE.ch()){
            arr_direct=addToArray(arr_direct,direct.inverted().clockwise().clockwise());
        }
        if(getCharInDirect(direct.inverted().clockwise().clockwise().clockwise(), point_snake)==Elements.NONE.ch()){
            arr_direct=addToArray(arr_direct,direct.inverted().clockwise().clockwise().clockwise());
        }

        if(arr_direct.length==3) {
            point_nextStep0 = arr_direct[0].change(point_snake);
            point_nextStep1 = arr_direct[1].change(point_snake);
            point_nextStep2 = arr_direct[2].change(point_snake);
            if (point_nextStep0.distance(point_exit) > point_nextStep1.distance(point_exit)) {
                if (point_nextStep0.distance(point_exit) > point_nextStep2.distance(point_exit)) {
                    direct = arr_direct[0];
                } else {
                    direct = arr_direct[2];
                }
            } else {
                if (point_nextStep1.distance(point_exit) > point_nextStep2.distance(point_exit)) {
                    direct = arr_direct[1];
                } else {
                    direct = arr_direct[2];
                }
            }
        } else{
            if(arr_direct.length==2){
                point_nextStep0 = arr_direct[0].change(point_snake);
                point_nextStep1 = arr_direct[1].change(point_snake);
                if (point_nextStep0.distance(point_exit) > point_nextStep1.distance(point_exit)) {
                    direct = arr_direct[0];
                } else {
                    direct = arr_direct[1];
                }
            }
            else{
                direct=arr_direct[0];
            }
        }

        return direct.toString();
    }

    private Direction searchDirect(Direction direct) {
        if (direct == Direction.STOP) {
            direct = searchLook(board.getField()[point_snake.getX()][point_snake.getY()]).inverted(); //при первом вхождении
        }else {
            setCharInDirect(direct, getLookForDirect(direct));
            point_snake.move(point_snake.getX() + direct.changeX(0), point_snake.getY() + direct.changeY(0));
            direct = direct.inverted(); // для исключения направления откуда пришда змейка
        }

        //outMass();

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

                char ch = getCharInDirect(arr, point_snake);

                if (ch == '♣') {
                    return null;
                }

                Direction direction = getDirection(arr_direct, arr, ch);
                if (direction != null) {return direction;}
            }

            for (Direction arr : arr_NotPriorDirect) {     //проверяем возможность идти по не приоритетным направлениям

                char ch = getCharInDirect(arr, point_snake);

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
            //outMass();
            direct = searchDirect(arr);

            if (direct == Direction.ACT || direct == Direction.STOP || direct == null){
                moveBack(arr);
            }

            if (direct == Direction.ACT) {
                if (point_snake.itsMe(point_snake_copy)) {
                    outMass();
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
                if (arr[i] == searchLook(getCharInDirect(Direction.STOP, point_snake))) {
                    Direction direct = arr[i];
                    arr[i] = arr[0];
                    arr[0] = direct;
                }
            }
        }else {
            for (int i = 0; i < arr.length - 1; i++) {
                if (arr[i] == searchLook(getCharInDirect(Direction.STOP, point_snake))) {
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

    private boolean setAnchorL(Direction []arr_direct, Direction direct) {
        boolean flag = false;
        for (Direction d : arr_direct) {
            if (!d.equals(direct)) {
                if (setCharInDirectL(d, '♣')) {
                    flag = true;
                }
            }
        }
        return flag;
    }

    private boolean setCharInDirectL(Direction direct, char symbol) {
        char ch = getCharInDirect(direct, point_snake);
        if (ch == Elements.NONE.ch()) {
            //board.set(point_snake.getX() + direct.changeX(0), point_snake.getY() + direct.changeY(0), symbol);
            return true;
        }
        if (ch == '♥') {
            board.set(point_snake.getX() + direct.changeX(0), point_snake.getY() + direct.changeY(0), ' ');
        }
        return false;
    }

    private void wipeoffAnchor(Direction []arr_direct, Direction direct) {
        for (Direction d : arr_direct) {
            if (!d.equals(direct)) {
                setCharInDirectA(d, ' ');
                //outMass();
            }
        }
    }

    private void wipeoffLook(Direction direct) {
        if (getCharInDirect(direct, point_snake) != '☺') {
            board.set(point_snake.getX() + direct.changeX(0), point_snake.getY() + direct.changeY(0), ' ');
        }
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

    private char getTailForDirect(Direction direct) {
        if (Direction.RIGHT == direct) {
            return '╘';
        }
        if (Direction.LEFT == direct) {
            return '╕';
        }
        if (Direction.DOWN == direct) {
            return '╓';
        }
        if (Direction.UP == direct) {
            return '╙';
        }
        return ' ';
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

    private char getCharInDirect(Direction direct, Point point) {
        return board.getField()[point.getX() +
                direct.changeX(0)][point.getY() + direct.changeY(0)];
    }

    private Point getPointInDirect(Direction direct, Point point) {
        return new PointImpl(point.getX() + direct.changeX(0),point.getY() + direct.changeY(0));
    }

    private Direction[] addToArray(Direction[] array, Direction s) {
        Direction[] ans = new Direction[array.length + 1];
        System.arraycopy(array, 0, ans, 0, array.length);
        ans[ans.length - 1] = s;
        return ans;
    }

    private boolean setCharInDirect(Direction direct, char symbol) {
        char ch = getCharInDirect(direct, point_snake);
        if (ch == Elements.NONE.ch()) {
            board.set(point_snake.getX() + direct.changeX(0), point_snake.getY() + direct.changeY(0), symbol);
            return true;
        }
        if (ch == '♥') {
            board.set(point_snake.getX() + direct.changeX(0), point_snake.getY() + direct.changeY(0), ' ');
        }
        return false;
    }

    private boolean setCharInDirectQ(Direction direct, char symbol) {
        char ch = getCharInDirect(direct, point_snake);
        if (ch == Elements.NONE.ch() || ch == '►' || ch == '◄' || ch == '▲' || ch == '▼') {
            board.set(point_snake.getX() + direct.changeX(0), point_snake.getY() + direct.changeY(0), symbol);
            return true;
        }
        return false;
    }

    private boolean setCharInDirectA(Direction direct, char symbol) {
        char ch = getCharInDirect(direct, point_snake);
        if (ch == Elements.NONE.ch() || ch == '♣') {
            board.set(point_snake.getX() + direct.changeX(0), point_snake.getY() + direct.changeY(0), symbol);
            return true;
        }
        return false;
    }

    public Direction[] getDirectionSnake(Board board,Point point) {
        Direction[] directions = new Direction[0];
        Direction[] pointDirection = board.getAt(point.getX(),point.getY()).getDirectionElement();
        boolean flag = false;
        for (Direction d : pointDirection) {
            for (Direction p : board.getAt(point.getX() + d.changeX(0), point.getY() + d.changeY(0)).getDirectionElement()) {
                if (d == p.inverted()) {
                    directions = addToArray(directions, d);
                    flag = true;
                }
            }
        }
        if (flag) {
            return directions;
        } else {
            return null;
        }
    }

}
