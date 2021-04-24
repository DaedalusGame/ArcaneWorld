package party.lemons.arcaneworld.gen.dungeon.generation;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import party.lemons.arcaneworld.ArcaneWorld;
import party.lemons.lemonlib.ticker.TickerHandler;

import java.util.*;

/**
 * Created by Sam on 20/09/2018.
 */
public class DungeonGenerator
{
    private static Map<String, List<ResourceLocation>> cachedTemplates = new HashMap<>();

    private static final int MIN_WIDTH = 5;
    private static final int MIN_HEIGHT = 5;
    private static final int MAX_WIDTH = 10;
    private static final int MAX_HEIGHT = 10;
    public static final int ROOM_WIDTH = 13;

    private BlockPos origin;
    private World world;
    private Random random;
    private int width, height;
    private RoomDirection[][] directions;

    public DungeonGenerator(World world, BlockPos origin)
    {
        this.world = world;
        this.origin = origin;
        this.random = world.rand;
        this.width =  MIN_WIDTH + random.nextInt(MAX_WIDTH - MIN_WIDTH + 1);
        this.height =  MIN_HEIGHT + random.nextInt(MAX_HEIGHT - MIN_HEIGHT + 1);

        directions = new RoomDirection[width][height];
        for (int i = 0; i < width; i++)
        {
            for (int j = 0; j < height; j++)
            {
                directions[i][j] = new RoomDirection();
            }
        }
    }

    public void generate()
    {
        createLayout();
        TickerHandler.addTicker(new TickerDungeon(world, this), world.provider.getDimension());
    }

    public boolean generateRoom(int x, int y)
    {
        RoomDirection direction = directions[x][y];
        if (!direction.isSealed())
        {
            BlockPos generatePos = origin.add(x * ROOM_WIDTH, 0, y * ROOM_WIDTH);
            Rotation rotation = direction.getRotation();

            int offsetX = 0;
            int offsetZ = 0;
            switch (rotation)
            {
                case CLOCKWISE_90:
                    offsetX = 12;
                    break;
                case CLOCKWISE_180:
                    offsetX = 12;
                    offsetZ = 12;
                    break;
                case COUNTERCLOCKWISE_90:
                    offsetZ = 12;
                    break;
                default:
                    break;
            }

            PlacementSettings settings = new PlacementSettings().setRotation(direction.getRotation()).setMirror(direction.getMirror());
            ResourceLocation layout;

            if (x == 0 && y == 0)
            {
                switch (direction.getShape())
                {
                    case OPEN:
                        layout = new ResourceLocation(ArcaneWorld.MODID, "dungeon/start/open_start_1");
                        break;
                    case CORNER:
                        layout = new ResourceLocation(ArcaneWorld.MODID, "dungeon/start/corner_start_1");
                        break;
                    case T:
                        layout = new ResourceLocation(ArcaneWorld.MODID, "dungeon/start/t_start_1");
                        break;
                    case CORRIDOR:
                        layout = new ResourceLocation(ArcaneWorld.MODID, "dungeon/start/corridor_start_1");
                        break;
                    case CAP:
                        layout = new ResourceLocation(ArcaneWorld.MODID, "dungeon/start/cap_start_1");
                        break;
                    default:
                        layout = new ResourceLocation(ArcaneWorld.MODID, "dungeon/start/end_start_1");
                        break;
                }
            }
            else if (x == width - 1 && y == height -1)
            {
                switch (direction.getShape())
                {
                    case OPEN:
                        layout = new ResourceLocation(ArcaneWorld.MODID, "dungeon/portals/open_end_1");
                        break;
                    case CORNER:
                        layout = new ResourceLocation(ArcaneWorld.MODID, "dungeon/portals/corner_end_1");
                        break;
                    case T:
                        layout = new ResourceLocation(ArcaneWorld.MODID, "dungeon/portals/t_end_1");
                        break;
                    case CORRIDOR:
                        layout = new ResourceLocation(ArcaneWorld.MODID, "dungeon/portals/corridor_end_1");
                        break;
                    case CAP:
                        layout = new ResourceLocation(ArcaneWorld.MODID, "dungeon/portals/end_cap_1");
                        break;
                    default:
                        layout = new ResourceLocation(ArcaneWorld.MODID, "dungeon/portals/end_cap_1");
                        break;
                }
            }
            else
            {
                layout = getRoomLayout(direction);
            }
            Template template = world.getSaveHandler().getStructureTemplateManager().getTemplate(world.getMinecraftServer(), layout);
            template.addBlocksToWorld(world, generatePos.add(offsetX, 0, offsetZ), new DungeonRoomProcessor(), settings, 2);

            return true;
        }

        return false;
    }

    private void createLayout()
    {
        int x = 0;
        int z = 0;
        boolean finished = false;
        while (!finished)
        {
            RoomDirection currentRoom = directions[x][z];
            if (z != height - 1)
            {
                Direction moveDirection = random.nextBoolean() ? Direction.EAST : Direction.WEST;
                if (!(x + moveDirection.getX() > 0 && x + moveDirection.getX() < width))
                {
                   moveDirection = Direction.SOUTH;
                }
                currentRoom.withDirection(moveDirection, true);
                directions[x + moveDirection.getX()][ z + moveDirection.getY()].withDirection(moveDirection.opposite(), true);
                x += moveDirection.getX();
                z += moveDirection.getY();
            }
            else
            {
                if (x != width - 1)
                {
                    currentRoom.withDirection(Direction.EAST, true);
                    directions[x +1][z].withDirection(Direction.WEST, true);

                    x += 1;
                }
                else
                {
                    finished = true;
                }
            }
        }

        for (int i = 0; i < 40; i++)
        {
            int mutateX =  random.nextInt(width);
            int mutateZ =  random.nextInt(height);

            RoomDirection direction = directions[mutateX][mutateZ];
            Direction mutateDir = Direction.random();

            if (direction.isSealed())
                continue;
            if (mutateX == 0 && mutateDir.getX() < 0)
                continue;
            if (mutateX == width - 1 && mutateDir.getX() > 0)
                continue;
            if (mutateZ == 0 && mutateDir.getY() < 0)
                continue;
            if (mutateZ == height - 1 && mutateDir.getY() > 0)
                continue;
            Direction moveDirection = mutateDir;

            direction.withDirection(moveDirection, true);
            directions[mutateX + mutateDir.getX()][mutateZ + mutateDir.getY()].withDirection(moveDirection.opposite(), true);
        }
    }

    public ResourceLocation getRoomLayout(RoomDirection direction)
    {
        String directory = direction.getDirectory();
        if (!cachedTemplates.containsKey(directory))
        {
            List<ResourceLocation> locations = new ArrayList<>();
            switch (directory)
            {
                case "cap":
                    loadTemplates(locations, directory, 15);
                break;
                case "corner":
                    loadTemplates(locations, directory, 5);
                    break;
                case "corridor":
                    loadTemplates(locations, directory, 10);
                    break;
                case "open":
                    loadTemplates(locations, directory, 1);
                    break;
                case "sealed":
                    locations.add(new ResourceLocation(ArcaneWorld.MODID, "dungeon/" + directory + "/" + directory));
                    break;
                case "t":
                    loadTemplates(locations, directory, 11);
                    break;
            }

          /*  try
            {
               /* File file = new File(this.getClass().getClassLoader().getResource("assets/" + ArcaneWorld.MODID + "/structures/dungeon/" + directory).toURI());
                File[] templates = file.listFiles();

                for (File files : templates)
                {
                    if (!files.isDirectory())
                    {
                        String name = files.getName().substring(0, files.getName().lastIndexOf("."));
                        locations.add(new ResourceLocation(ArcaneWorld.MODID, "dungeon/" + directory + "/" + name));
                    }
                }
            }catch (URISyntaxException e)
            {
                e.printStackTrace();
            }
        */
            cachedTemplates.put(directory, locations);
        }

        List<ResourceLocation> selectFrom = cachedTemplates.get(directory);
        return selectFrom.get(random.nextInt(selectFrom.size()));
    }

    private void loadTemplates(List<ResourceLocation> locations, String directory, int size)
    {
        for (int i = 1; i < size + 1; i++)
        {
            locations.add((new ResourceLocation(ArcaneWorld.MODID, "dungeon/" + directory + "/" + directory + "_" + i)));
        }
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }
}
