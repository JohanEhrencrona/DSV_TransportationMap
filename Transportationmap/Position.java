//Johan Ehrencrona joeh2789

public class Position {
    private int coordinateX;
    private int coordinateY;

    public Position(int x, int y){
        this.coordinateY = y;
        this.coordinateX = x;
    }

    public int getCoordinateX() {
        return coordinateX;
    }

    public int getCoordinateY() {
        return coordinateY;
    }

    @Override
    public int hashCode(){
        return (coordinateX *17)*(coordinateY *31);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null) return false;
        if (this.getClass() != other.getClass()) return false;
        Position pos = (Position) other;
        return coordinateX == pos.coordinateX && coordinateY == pos.coordinateY;
    }
}
