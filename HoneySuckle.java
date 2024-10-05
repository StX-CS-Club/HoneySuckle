public class HoneySuckle {
    static void main(String[] args){
        int width = 51;
        int height = 100;
        int start = (width-1)/2;

        int[][] grid = new int[width][height];

        grid[start-1][height-1]=1;
        grid[start][height-1]=1;
        grid[start+1][height-1]=1;

        grid[start-1][0]=1;
        grid[start][0]=1;
        grid[start+1][0]=1;

        for(int x = 0; x < start; x++){
            for(int y = height-2; y > 0; y--){
                int leftProb = 10+55*grid[start-x+1][y]+30*grid[start-x][y+1];
                int rightProb = 10+55*grid[start+x-1][y]+30*grid[start-x][y+1];
                if(Math.random() * 100 <= leftProb){
                    grid[start-x][y] = 1;
                }
                if(Math.random() * 100 <= rightProb){
                    grid[start+x][y] = 1;
                }
            }
        }

        for(int y = 0; y < height; y++){
            String result = "";
            for(int x = 0; x < width; x++){
                if(grid[x][y] == 1){
                    result = result+" ";
                } else {
                    result = result+"■";
                }
            }
            System.out.println(result);
        }
    }
}
