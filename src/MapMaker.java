import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

class Constants
{
	static final String IMAGE_FOLDER = "Images";
	static final String MAP_FOLDER = "Maps";
	static final String DATA_FOLDER = "Data";
	static final String MONSTER_FOLDER = "Monsters";
	static final String DEITIES_FOLDER = "Deities";
	static final String ENCOUNTER_FOLDER = "Encounters";
}

class Map implements ActionListener
{
	static final int NPCS = 5;
	final int TOWN_MIN_TAVERNS = 1;
	final int TOWN_MAX_TAVERNS = 2;
	final int CITY_MIN_TAVERNS = 2;
	final int CITY_MAX_TAVERNS = 4;
	final int CASTLE_TAVERNS = 1;
	final int CITY_LANDMARKS = 4;
	final int FORTRESS_LANDMARKS = 1;

	final int SQUARE_SIZE = 20;
	final int CODE_SIZE = 3;
	final int HUD_GRID_WIDTH = 4;
	final int HUD_GRID_HEIGHT = 4;
	final int SAVE_DEPTH = 42;
	int horizontal_size, vertical_size, horizontal_grid_size, vertical_grid_size;
	Terrain[][] terrain_grid;
	Map_Object[][] object_grid;
	HUD_Object[][] draw_grid;
	HUD_Object[][] HUD_array;
	String[][][] save_file;
	String[][] hud_save;
	ArrayList<Map_Object> actors = new ArrayList<Map_Object>();

	ArrayList<ArrayList<String>> deity_races = new ArrayList<ArrayList<String>>();

	ArrayList<String> city_names = new ArrayList<String>();
	ArrayList<String> town_names = new ArrayList<String>();
	ArrayList<String> fortress_names = new ArrayList<String>();
	ArrayList<String> npc_names = new ArrayList<String>();
	ArrayList<String> ruler_types = new ArrayList<String>();
	ArrayList<String> npc_jobs = new ArrayList<String>();
	ArrayList<String> landmark_names = new ArrayList<String>();
	ArrayList<String> races = new ArrayList<String>();
	ArrayList<String> city_races_common = new ArrayList<String>();
	ArrayList<String> city_races_uncommon = new ArrayList<String>();
	ArrayList<String> fortress_races_common = new ArrayList<String>();
	ArrayList<String> fortress_races_uncommon = new ArrayList<String>();
	ArrayList<String> town_races_common = new ArrayList<String>();
	ArrayList<String> town_races_uncommon = new ArrayList<String>();
	ArrayList<String> farm_races = new ArrayList<String>();
	ArrayList<String> mine_races = new ArrayList<String>();
	ArrayList<String> temple_races = new ArrayList<String>();
	ArrayList<String> temple_deities = new ArrayList<String>();
	ArrayList<String> deities = new ArrayList<String>();
	ArrayList<String> monastery_races = new ArrayList<String>();
	ArrayList<String> quests = new ArrayList<String>();
	ArrayList<String> city_landmarks = new ArrayList<String>();
	ArrayList<String> tavern_names = new ArrayList<String>();
	ArrayList<String> tavern_types = new ArrayList<String>();
	ArrayList<String> tavern_sizes = new ArrayList<String>();
	ArrayList<String> events = new ArrayList<String>();
	ArrayList<String> camps = new ArrayList<String>();

	int HUD_hpos;
	int HUD_vpos;
	PrintWriter out;
	String selected_terrain = "";
	String line;

	boolean edit_mode = false;
	boolean DMmode = false;

	public Map(int hSize, int vSize)
	{
		horizontal_size = hSize;
		vertical_size = vSize;
		horizontal_grid_size = horizontal_size/SQUARE_SIZE;
		vertical_grid_size = vertical_size/SQUARE_SIZE;
		terrain_grid = new Terrain[horizontal_grid_size][vertical_grid_size]; //60x30 grid
		draw_grid = new HUD_Object[horizontal_grid_size][vertical_grid_size]; //60x30 grid
		object_grid = new Map_Object[horizontal_grid_size][vertical_grid_size];
		HUD_array = new HUD_Object[HUD_GRID_WIDTH][HUD_GRID_HEIGHT];
		save_file = new String[horizontal_grid_size][vertical_grid_size][SAVE_DEPTH];
		hud_save = new String[HUD_GRID_WIDTH][HUD_GRID_HEIGHT];
		HUD_hpos = horizontal_size+20;
		HUD_vpos = 0;
		readLists();
		for(int i = 0; i < horizontal_grid_size; i++)
			for(int j = 0; j < vertical_grid_size; j++)
			{
				terrain_grid[i][j] = null;
				object_grid[i][j] = null;
				draw_grid[i][j] = null;
				for(int k = 0; k < SAVE_DEPTH; k++)
					save_file[i][j][k] = "";
			}

		for(int i = 0; i < HUD_GRID_WIDTH; i++)
			for(int j = 0; j < HUD_GRID_HEIGHT; j++)
			{
				HUD_array[i][j] = null;
				hud_save[i][j] = "";
			}

		load();

		HUD_array[0][0] = new HUD_Grass(this, HUD_hpos, HUD_vpos);
		hud_save[0][0] = Grass.getCode();
		HUD_array[1][0] = new HUD_Water(this, HUD_hpos+20*1, HUD_vpos);
		hud_save[1][0] = Water.getCode();
		HUD_array[2][0] = new HUD_Dirt(this, HUD_hpos+20*2, HUD_vpos);
		hud_save[2][0] = Dirt.getCode();
		HUD_array[0][1] = new HUD_Sand(this, HUD_hpos, HUD_vpos+20);
		hud_save[0][1] = Sand.getCode();
		HUD_array[2][1] = new HUD_Mountain(this, HUD_hpos+20*2, HUD_vpos+20);
		hud_save[2][1] = Mountain.getCode();
		HUD_array[1][1] = new HUD_Snow(this, HUD_hpos+20, HUD_vpos+20);
		hud_save[1][1] = Snow.getCode();
		HUD_array[3][1] = new HUD_Tree(this, HUD_hpos+20*3, HUD_vpos+20);
		hud_save[3][1] = Tree.getCode();

		HUD_array[0][2] = new HUD_City(this, HUD_hpos, HUD_vpos+20*2);
		hud_save[0][2] = City.getCode();
		HUD_array[1][2] = new HUD_Fortress(this, HUD_hpos+20, HUD_vpos+20*2);
		hud_save[1][2] = Fortress.getCode();
		HUD_array[2][2] = new HUD_Town(this, HUD_hpos+20*2, HUD_vpos+20*2);
		hud_save[2][2] = Town.getCode();
		HUD_array[3][2] = new HUD_Camp(this, HUD_hpos+20*3, HUD_vpos+20*2);
		hud_save[3][2] = Camp.getCode();
		HUD_array[0][3] = new HUD_Landmark(this, HUD_hpos, HUD_vpos+20*3);
		hud_save[0][3] = Landmark.getCode();
	}

	public void fillEllipse(int x1, int y1, int x2, int y2)
	{
		int a = (x2-x1)/2;
		int b = (y2-y1)/2;
		int centrex = x1+a;
		int centrey = y1+b;
		for(int i = 0; i < a; i++)
		{
			//bottom right
			int locx = centrex+i;
			int locy = centrey+(int)Math.round(b*Math.sqrt(1 - Math.pow(i, 2)/a/a));
			for(int j = 0; locy>centrey; j++)
			{
				locy = centrey+(int)Math.round(b*Math.sqrt(1 - Math.pow(i, 2)/a/a))-j;
				placeObject(locx, locy);
//				terrain_grid[locx][locy] = new Dirt(this, locx*20, locy*20);
			}

			//bottom left
			locx = centrex-i;
			locy = centrey+(int)Math.round(b*Math.sqrt(1 - Math.pow(i, 2)/a/a));
			for(int j = 0; locy>centrey; j++)
			{
				locy = centrey+(int)Math.round(b*Math.sqrt(1 - Math.pow(i, 2)/a/a))-j;
				placeObject(locx, locy);
//				terrain_grid[locx][locy] = new Dirt(this, locx*20, locy*20);
			}

			//top left
			locx = centrex-i;
			locy=centrey-(int)Math.round(b*Math.sqrt(1- Math.pow(i, 2)/a/a));
			for(int j = 0; locy<centrey; j++)
			{
				locy=centrey-(int)Math.round(b*Math.sqrt(1- Math.pow(i, 2)/a/a))+j;
				placeObject(locx, locy);
//				terrain_grid[locx][locy] = new Dirt(this, locx*20, locy*20);
			}

			//top right
			locx = centrex+i;
			locy = centrey-(int)Math.round(b*Math.sqrt(1 - Math.pow(i, 2)/a/a));
			for(int j = 0; locy<centrey; j++)
			{
				locy = centrey-(int)Math.round(b*Math.sqrt(1 - Math.pow(i, 2)/a/a))+j;
				placeObject(locx, locy);
//				terrain_grid[locx][locy] = new Dirt(this, locx*20, locy*20);
			}
		}
		for(int i = 0; i < b; i++)
		{
			//bottom right
			int locy = centrey+i;
			int locx = centrex+(int)Math.round(a*Math.sqrt(1 - Math.pow(i, 2)/b/b));
			for(int j = 0; locx>centrex; j++)
			{
				locx = centrex+(int)Math.round(a*Math.sqrt(1 - Math.pow(i, 2)/b/b))-j;
				placeObject(locx, locy);
//				terrain_grid[locx][locy] = new Dirt(this, locx*20, locy*20);
			}

			//bottom left
			locy = centrey+i;
			locx = centrex-(int)Math.round(a*Math.sqrt(1 - Math.pow(i, 2)/b/b));
			for(int j = 0; locx<centrex; j++)
			{
				locx = centrex-(int)Math.round(a*Math.sqrt(1 - Math.pow(i, 2)/b/b))+j;
				placeObject(locx, locy);
//				terrain_grid[locx][locy] = new Dirt(this, locx*20, locy*20);
			}

			//top left
			locy = centrey-i;
			locx = centrex-(int)Math.round(a*Math.sqrt(1 - Math.pow(i, 2)/b/b));
			for(int j = 0; locx<centrex; j++)
			{
				locx = centrex-(int)Math.round(a*Math.sqrt(1 - Math.pow(i, 2)/b/b))+j;
				placeObject(locx, locy);
//				terrain_grid[locx][locy] = new Dirt(this, locx*20, locy*20);
			}

			//top right
			locy = centrey-i;
			locx = centrex+(int)Math.round(a*Math.sqrt(1 - Math.pow(i, 2)/b/b));
			for(int j = 0; locx>centrex; j++)
			{
				locx = centrex+(int)Math.round(a*Math.sqrt(1 - Math.pow(i, 2)/b/b))-j;
				placeObject(locx, locy);
//				terrain_grid[locx][locy] = new Dirt(this, locx*20, locy*20);
			}
		}
		clearDrawGrid();
	}

	public void fillCircle(int x1, int y1, int x2, int y2)
	{
		int r = (y2-y1)/2;
		int centrex = x1+r;
		int centrey = y1+r;
		for(int i = 0; i < r; i++)
		{
			//bottom right
			int locx = centrex+i;
			int locy = centrey+(int)Math.round(Math.sqrt(Math.pow(r, 2) - Math.pow(i, 2)));
			for(int j = 0; locy>centrey; j++)
			{
				locy = centrey+(int)Math.round(Math.sqrt(Math.pow(r, 2) - Math.pow(i, 2)))-j;
				placeObject(locx, locy);
//				terrain_grid[locx][locy] = new Dirt(this, locx*20, locy*20);
			}
			locy = centrey+i;
			locx = centrex+(int)Math.round(Math.sqrt(Math.pow(r, 2) - Math.pow(i, 2)));
			for(int j = 0; locx>centrex; j++)
			{
				locx = centrex+(int)Math.round(Math.sqrt(Math.pow(r, 2) - Math.pow(i, 2)))-j;
				placeObject(locx, locy);
//				terrain_grid[locx][locy] = new Dirt(this, locx*20, locy*20);
			}

			//bottom left
			locy = centrey+i;
			locx = centrex-(int)Math.round(Math.sqrt(Math.pow(r, 2) - Math.pow(i, 2)));
			for(int j = 0; locx<centrex; j++)
			{
				locx = centrex-(int)Math.round(Math.sqrt(Math.pow(r, 2) - Math.pow(i, 2)))+j;
				placeObject(locx, locy);
//				terrain_grid[locx][locy] = new Dirt(this, locx*20, locy*20);
			}
			locx = centrex-i;
			locy = centrey+(int)Math.round(Math.sqrt(Math.pow(r, 2) - Math.pow(i, 2)));
			for(int j = 0; locy>centrey; j++)
			{
				locy = centrey+(int)Math.round(Math.sqrt(Math.pow(r, 2) - Math.pow(i, 2)))-j;
				placeObject(locx, locy);
//				terrain_grid[locx][locy] = new Dirt(this, locx*20, locy*20);
			}

			//top left
			locy = centrey-i;
			locx = centrex-(int)Math.round(Math.sqrt(Math.pow(r, 2) - Math.pow(i, 2)));
			for(int j = 0; locx<centrex; j++)
			{
				locx = centrex-(int)Math.round(Math.sqrt(Math.pow(r, 2) - Math.pow(i, 2)))+j;
				placeObject(locx, locy);
//				terrain_grid[locx][locy] = new Dirt(this, locx*20, locy*20);
			}
			locx = centrex-i;
			locy=centrey-(int)Math.round(Math.sqrt(Math.pow(r, 2) - Math.pow(i, 2)));
			for(int j = 0; locy<centrey; j++)
			{
				locy=centrey-(int)Math.round(Math.sqrt(Math.pow(r, 2) - Math.pow(i, 2)))+j;
				placeObject(locx, locy);
//				terrain_grid[locx][locy] = new Dirt(this, locx*20, locy*20);
			}

			//top right
			locx = centrex+i;
			locy = centrey-(int)Math.round(Math.sqrt(Math.pow(r, 2) - Math.pow(i, 2)));
			for(int j = 0; locy<centrey; j++)
			{
				locy = centrey-(int)Math.round(Math.sqrt(Math.pow(r, 2) - Math.pow(i, 2)))+j;
				placeObject(locx, locy);
//				terrain_grid[locx][locy] = new Dirt(this, locx*20, locy*20);
			}
			locy = centrey-i;
			locx = centrex+(int)Math.round(Math.sqrt(Math.pow(r, 2) - Math.pow(i, 2)));
			for(int j = 0; locx>centrex; j++)
			{
				locx = centrex+(int)Math.round(Math.sqrt(Math.pow(r, 2) - Math.pow(i, 2)))-j;
				placeObject(locx, locy);
//				terrain_grid[locx][locy] = new Dirt(this, locx*20, locy*20);
			}
		}
		clearDrawGrid();
	}

	public void drawCircle(int x, int y, int r)
	{
		int centrex = x+r;
		int centrey = y+r;
		for(int i = 0; i < r; i++)
		{
			//bottom right
			int locx = centrex+i;
			int locy = centrey+(int)Math.round(Math.sqrt(Math.pow(r, 2) - Math.pow(i, 2)));
			terrain_grid[locx][locy] = new Dirt(this, locx*20, locy*20);
			locy = centrey+i;
			locx = centrex+(int)Math.round(Math.sqrt(Math.pow(r, 2) - Math.pow(i, 2)));
			terrain_grid[locx][locy] = new Dirt(this, locx*20, locy*20);

			//bottom left
			locy = centrey+i;
			locx = centrex-(int)Math.round(Math.sqrt(Math.pow(r, 2) - Math.pow(i, 2)));
			terrain_grid[locx][locy] = new Dirt(this, locx*20, locy*20);
			locx = centrex-i;
			locy = centrey+(int)Math.round(Math.sqrt(Math.pow(r, 2) - Math.pow(i, 2)));
			terrain_grid[locx][locy] = new Dirt(this, locx*20, locy*20);

			//top left
			locy = centrey-i;
			locx = centrex-(int)Math.round(Math.sqrt(Math.pow(r, 2) - Math.pow(i, 2)));
			terrain_grid[locx][locy] = new Dirt(this, locx*20, locy*20);
			locx = centrex-i;
			locy=centrey-(int)Math.round(Math.sqrt(Math.pow(r, 2) - Math.pow(i, 2)));
			terrain_grid[locx][locy] = new Dirt(this, locx*20, locy*20);

			//top right
			locx = centrex+i;
			locy = centrey-(int)Math.round(Math.sqrt(Math.pow(r, 2) - Math.pow(i, 2)));
			terrain_grid[locx][locy] = new Dirt(this, locx*20, locy*20);
			locy = centrey-i;
			locx = centrex+(int)Math.round(Math.sqrt(Math.pow(r, 2) - Math.pow(i, 2)));
			terrain_grid[locx][locy] = new Dirt(this, locx*20, locy*20);
		}
	}

	public void randomize()
	{
		for(int i = 0; i < horizontal_grid_size; i++)
		{
			for(int j = 0; j < vertical_grid_size; j++)
			{
				if(terrain_grid[i][j] instanceof Grass)
				{
					/*
					 *Fortress: 1
					 *City: 2
					 *Town/Camp/Landmark: 3
					 */
					if((int)(Math.random()*10+1) == 1)
					{
						int random = (int)(Math.random()*12+1);
						if(random == 1)
						{
							terrain_grid[i][j] = createNewFortress(i, j);
							save_file[i][j][0] = Fortress.CODE;
						}
						else if(random >= 2 && random <= 3)
						{
							terrain_grid[i][j] = createNewCity(i, j);
							save_file[i][j][0] = City.CODE;
						}
						else if(random >= 4 && random <= 6)
						{
							terrain_grid[i][j] = createNewTown(i, j);
							save_file[i][j][0] = Town.CODE;
						}
						else if(random >= 7 && random <= 10)
						{
							terrain_grid[i][j] = createNewCamp(i, j);
							save_file[i][j][0] = Camp.CODE;
						}
						else if(random >= 11 && random <= 12)
						{
							terrain_grid[i][j] = createNewLandmark(i, j);
							save_file[i][j][0] = Landmark.CODE;
						}
					}
				}
			}
		}
	}

	private City createNewCity(int x, int y)
	{
		int num_taverns = (int)(Math.random()*CITY_MAX_TAVERNS+1);
		if(num_taverns < CITY_MIN_TAVERNS)
			num_taverns = CITY_MIN_TAVERNS;
		String name = city_names.get((int)(Math.random()*city_names.size()));
		int race_num = (int)(Math.random()*3+1);
		String pred_race;
		if(race_num == 3)
			pred_race = city_races_uncommon.get((int)(Math.random()*city_races_uncommon.size()));
		else
			pred_race = city_races_common.get((int)(Math.random()*city_races_common.size()));
		String not_building = "";

		if(!pred_race.equals("Elf") && !pred_race.equals("Eladrin"))
		{
			int num_landmarks = (int)(Math.random()*CITY_LANDMARKS+1);

			for(int i = 0; i < num_landmarks; i++)
			{
				String tmpstr = city_landmarks.get((int)(Math.random()*city_landmarks.size()));
				if(tmpstr.toLowerCase().contains("temple"))
					tmpstr += " of " + temple_deities.get((int)(Math.random()*temple_deities.size()));
				while(not_building.toLowerCase().contains(tmpstr.toLowerCase()))
					tmpstr = city_landmarks.get((int)(Math.random()*city_landmarks.size()));
				not_building += tmpstr;
				if(i != num_landmarks-1)
					not_building += ", ";
			}
		}

		String t1name = "";
		String t1type = "";
		String t1size = "";
		String t1owner = "";
		String t1race = "";

		if(!pred_race.equals("Elf") && !pred_race.equals("Eladrin"))
		{
			t1name = tavern_names.get((int)(Math.random()*tavern_names.size()));
			t1type = tavern_types.get((int)(Math.random()*tavern_types.size()));
			t1size = tavern_sizes.get((int)(Math.random()*tavern_sizes.size()));
			t1owner = npc_names.get((int)(Math.random()*npc_names.size()));
			t1race = races.get((int)(Math.random()*races.size()));
		}
		else
		{
			num_taverns = 0;
		}

		String t2name;
		String t2type;
		String t2size;
		String t2owner;
		String t2race;

		String t3name;
		String t3type;
		String t3size;
		String t3owner;
		String t3race;

		String t4name;
		String t4type;
		String t4size;
		String t4owner;
		String t4race;

		if(num_taverns > 1)
		{
			t2name = tavern_names.get((int)(Math.random()*tavern_names.size()));
			t2type = tavern_types.get((int)(Math.random()*tavern_types.size()));
			t2size = tavern_sizes.get((int)(Math.random()*tavern_sizes.size()));
			t2owner = npc_names.get((int)(Math.random()*npc_names.size()));
			t2race = races.get((int)(Math.random()*races.size()));
		}
		else
		{
			t2name = "";
			t2type = "";
			t2size = "";
			t2owner = "";
			t2race = "";
		}
		if(num_taverns > 2)
		{
			t3name = tavern_names.get((int)(Math.random()*tavern_names.size()));
			t3type = tavern_types.get((int)(Math.random()*tavern_types.size()));
			t3size = tavern_sizes.get((int)(Math.random()*tavern_sizes.size()));
			t3owner = npc_names.get((int)(Math.random()*npc_names.size()));
			t3race = races.get((int)(Math.random()*races.size()));
		}
		else
		{
			t3name = "";
			t3type = "";
			t3size = "";
			t3owner = "";
			t3race = "";
		}
		if(num_taverns > 3)
		{
			t4name = tavern_names.get((int)(Math.random()*tavern_names.size()));
			t4type = tavern_types.get((int)(Math.random()*tavern_types.size()));
			t4size = tavern_sizes.get((int)(Math.random()*tavern_sizes.size()));
			t4owner = npc_names.get((int)(Math.random()*npc_names.size()));
			t4race = races.get((int)(Math.random()*races.size()));
		}
		else
		{
			t4name = "";
			t4type = "";
			t4size = "";
			t4owner = "";
			t4race = "";
		}
		String rulername = ruler_types.get((int)(Math.random()*ruler_types.size())) + npc_names.get((int)(Math.random()*npc_names.size()));
		String n1name = npc_names.get((int)(Math.random()*npc_names.size()));
		String n1race = pred_race;
		String n1job = npc_jobs.get((int)(Math.random()*npc_jobs.size()));
		String n2name = npc_names.get((int)(Math.random()*npc_names.size()));
		String n2race = pred_race;
		String n2job = npc_jobs.get((int)(Math.random()*npc_jobs.size()));
		String n3name = npc_names.get((int)(Math.random()*npc_names.size()));
		String n3race = pred_race;
		String n3job = npc_jobs.get((int)(Math.random()*npc_jobs.size()));
		String n4name = npc_names.get((int)(Math.random()*npc_names.size()));
		String n4race = races.get((int)(Math.random()*races.size()));
		String n4job = npc_jobs.get((int)(Math.random()*npc_jobs.size()));
		String n5name = npc_names.get((int)(Math.random()*npc_names.size()));
		String n5race = races.get((int)(Math.random()*races.size()));
		String n5job = npc_jobs.get((int)(Math.random()*npc_jobs.size()));
		String qu1 = quests.get((int)(Math.random()*quests.size()));
		String qu2 = quests.get((int)(Math.random()*quests.size()));
		String qu3 = quests.get((int)(Math.random()*quests.size()));
		String qu4 = quests.get((int)(Math.random()*quests.size()));
		String qu5 = quests.get((int)(Math.random()*quests.size()));
		String qu6 = quests.get((int)(Math.random()*quests.size()));
		String qu7 = quests.get((int)(Math.random()*quests.size()));
		String qu8 = quests.get((int)(Math.random()*quests.size()));
		String qu9 = quests.get((int)(Math.random()*quests.size()));
		String qu10 = quests.get((int)(Math.random()*quests.size()));
		return new City(this, x*20, y*20,
								name, pred_race, not_building,
								t1name, t1type, t1size, t1owner, t1race,
								t2name, t2type, t2size, t2owner, t2race,
								t3name, t3type, t3size, t3owner, t3race,
								t4name, t4type, t4size, t4owner, t4race,
								rulername,
								n1name, n1race, n1job,
								n2name, n2race, n2job,
								n3name, n3race, n3job,
								n4name, n4race, n4job,
								n5name, n5race, n5job,
								qu1, qu2, qu3, qu4, qu5, qu6, qu7, qu8, qu9, qu10, "false", "true");
	}

	private Fortress createNewFortress(int x, int y)
	{
		int num_taverns = (int)(Math.random()*CASTLE_TAVERNS+1);
		String name = fortress_names.get((int)(Math.random()*fortress_names.size()));
		int race_num = (int)(Math.random()*3+1);
		String pred_race;
		if(race_num == 3)
			pred_race = fortress_races_uncommon.get((int)(Math.random()*fortress_races_uncommon.size()));
		else
			pred_race = fortress_races_common.get((int)(Math.random()*fortress_races_common.size()));
		String not_building = city_landmarks.get((int)(Math.random()*city_landmarks.size()));
		String t1name = tavern_names.get((int)(Math.random()*tavern_names.size()));
		String t1type = tavern_types.get((int)(Math.random()*tavern_types.size()));
		String t1size = tavern_sizes.get((int)(Math.random()*tavern_sizes.size()));
		String t1owner = npc_names.get((int)(Math.random()*npc_names.size()));
		String t1race = races.get((int)(Math.random()*races.size()));

		String t2name;
		String t2type;
		String t2size;
		String t2owner;
		String t2race;

		String t3name;
		String t3type;
		String t3size;
		String t3owner;
		String t3race;

		String t4name;
		String t4type;
		String t4size;
		String t4owner;
		String t4race;

		if(num_taverns > 1)
		{
			t2name = tavern_names.get((int)(Math.random()*tavern_names.size()));
			t2type = tavern_types.get((int)(Math.random()*tavern_types.size()));
			t2size = tavern_sizes.get((int)(Math.random()*tavern_sizes.size()));
			t2owner = npc_names.get((int)(Math.random()*npc_names.size()));
			t2race = races.get((int)(Math.random()*races.size()));
		}
		else
		{
			t2name = "";
			t2type = "";
			t2size = "";
			t2owner = "";
			t2race = "";
		}
		if(num_taverns > 2)
		{
			t3name = tavern_names.get((int)(Math.random()*tavern_names.size()));
			t3type = tavern_types.get((int)(Math.random()*tavern_types.size()));
			t3size = tavern_sizes.get((int)(Math.random()*tavern_sizes.size()));
			t3owner = npc_names.get((int)(Math.random()*npc_names.size()));
			t3race = races.get((int)(Math.random()*races.size()));
		}
		else
		{
			t3name = "";
			t3type = "";
			t3size = "";
			t3owner = "";
			t3race = "";
		}
		if(num_taverns > 3)
		{
			t4name = tavern_names.get((int)(Math.random()*tavern_names.size()));
			t4type = tavern_types.get((int)(Math.random()*tavern_types.size()));
			t4size = tavern_sizes.get((int)(Math.random()*tavern_sizes.size()));
			t4owner = npc_names.get((int)(Math.random()*npc_names.size()));
			t4race = races.get((int)(Math.random()*races.size()));
		}
		else
		{
			t4name = "";
			t4type = "";
			t4size = "";
			t4owner = "";
			t4race = "";
		}
		String rulername = npc_names.get((int)(Math.random()*npc_names.size()));
		String n1name = npc_names.get((int)(Math.random()*npc_names.size()));
		String n1race = pred_race;
		String n1job = npc_jobs.get((int)(Math.random()*npc_jobs.size()));
		String n2name = npc_names.get((int)(Math.random()*npc_names.size()));
		String n2race = pred_race;
		String n2job = npc_jobs.get((int)(Math.random()*npc_jobs.size()));
		String n3name = npc_names.get((int)(Math.random()*npc_names.size()));
		String n3race = pred_race;
		String n3job = npc_jobs.get((int)(Math.random()*npc_jobs.size()));
		String n4name = npc_names.get((int)(Math.random()*npc_names.size()));
		String n4race = pred_race;
		String n4job = npc_jobs.get((int)(Math.random()*npc_jobs.size()));
		String n5name = npc_names.get((int)(Math.random()*npc_names.size()));
		String n5race = races.get((int)(Math.random()*races.size()));
		String n5job = npc_jobs.get((int)(Math.random()*npc_jobs.size()));
		String qu1 = quests.get((int)(Math.random()*quests.size()));
		String qu2 = quests.get((int)(Math.random()*quests.size()));
		String qu3 = quests.get((int)(Math.random()*quests.size()));
		String qu4 = quests.get((int)(Math.random()*quests.size()));
		String qu5 = quests.get((int)(Math.random()*quests.size()));
		String qu6 = quests.get((int)(Math.random()*quests.size()));
		String qu7 = quests.get((int)(Math.random()*quests.size()));
		String qu8 = quests.get((int)(Math.random()*quests.size()));
		String qu9 = quests.get((int)(Math.random()*quests.size()));
		String qu10 = quests.get((int)(Math.random()*quests.size()));
		return new Fortress(this, x*20, y*20,
								name, pred_race, not_building,
								t1name, t1type, t1size, t1owner, t1race,
								t2name, t2type, t2size, t2owner, t2race,
								t3name, t3type, t3size, t3owner, t3race,
								t4name, t4type, t4size, t4owner, t4race,
								rulername,
								n1name, n1race, n1job,
								n2name, n2race, n2job,
								n3name, n3race, n3job,
								n4name, n4race, n4job,
								n5name, n5race, n5job,
								qu1, qu2, qu3, qu4, qu5, qu6, qu7, qu8, qu9, qu10, "false", "true");
	}

	private Town createNewTown(int x, int y)
	{
		int num_taverns = (int)(Math.random()*TOWN_MAX_TAVERNS+1);
		if(num_taverns < TOWN_MIN_TAVERNS)
			num_taverns = TOWN_MIN_TAVERNS;
		String name = town_names.get((int)(Math.random()*town_names.size()));
		int race_num = (int)(Math.random()*3+1);
		String pred_race;
		if(race_num == 3)
			pred_race = town_races_uncommon.get((int)(Math.random()*town_races_uncommon.size()));
		else
			pred_race = town_races_common.get((int)(Math.random()*town_races_common.size()));
		String not_building = "";

		String t1name = "";
		String t1type = "";
		String t1size = "";
		String t1owner = "";
		String t1race = "";

		if(!pred_race.equals("Elf") && !pred_race.equals("Eladrin"))
		{
			t1name = tavern_names.get((int)(Math.random()*tavern_names.size()));
			t1type = tavern_types.get((int)(Math.random()*tavern_types.size()));
			t1size = tavern_sizes.get((int)(Math.random()*tavern_sizes.size()));
			t1owner = npc_names.get((int)(Math.random()*npc_names.size()));
			t1race = races.get((int)(Math.random()*races.size()));
		}
		else
		{
			num_taverns = 0;
		}

		String t2name;
		String t2type;
		String t2size;
		String t2owner;
		String t2race;

		String t3name;
		String t3type;
		String t3size;
		String t3owner;
		String t3race;

		String t4name;
		String t4type;
		String t4size;
		String t4owner;
		String t4race;

		if(num_taverns > 1)
		{
			t2name = tavern_names.get((int)(Math.random()*tavern_names.size()));
			t2type = tavern_types.get((int)(Math.random()*tavern_types.size()));
			t2size = tavern_sizes.get((int)(Math.random()*tavern_sizes.size()));
			t2owner = npc_names.get((int)(Math.random()*npc_names.size()));
			t2race = races.get((int)(Math.random()*races.size()));
		}
		else
		{
			t2name = "";
			t2type = "";
			t2size = "";
			t2owner = "";
			t2race = "";
		}
		if(num_taverns > 2)
		{
			t3name = tavern_names.get((int)(Math.random()*tavern_names.size()));
			t3type = tavern_types.get((int)(Math.random()*tavern_types.size()));
			t3size = tavern_sizes.get((int)(Math.random()*tavern_sizes.size()));
			t3owner = npc_names.get((int)(Math.random()*npc_names.size()));
			t3race = races.get((int)(Math.random()*races.size()));
		}
		else
		{
			t3name = "";
			t3type = "";
			t3size = "";
			t3owner = "";
			t3race = "";
		}
		if(num_taverns > 3)
		{
			t4name = tavern_names.get((int)(Math.random()*tavern_names.size()));
			t4type = tavern_types.get((int)(Math.random()*tavern_types.size()));
			t4size = tavern_sizes.get((int)(Math.random()*tavern_sizes.size()));
			t4owner = npc_names.get((int)(Math.random()*npc_names.size()));
			t4race = races.get((int)(Math.random()*races.size()));
		}
		else
		{
			t4name = "";
			t4type = "";
			t4size = "";
			t4owner = "";
			t4race = "";
		}
		String rulername = "";
		String n1name = npc_names.get((int)(Math.random()*npc_names.size()));
		String n1race = pred_race;
		String n1job = npc_jobs.get((int)(Math.random()*npc_jobs.size()));
		String n2name = npc_names.get((int)(Math.random()*npc_names.size()));
		String n2race = pred_race;
		String n2job = npc_jobs.get((int)(Math.random()*npc_jobs.size()));
		String n3name = npc_names.get((int)(Math.random()*npc_names.size()));
		String n3race = races.get((int)(Math.random()*races.size()));
		String n3job = npc_jobs.get((int)(Math.random()*npc_jobs.size()));
		String n4name = npc_names.get((int)(Math.random()*npc_names.size()));
		String n4race = races.get((int)(Math.random()*races.size()));
		String n4job = npc_jobs.get((int)(Math.random()*npc_jobs.size()));
		String n5name = npc_names.get((int)(Math.random()*npc_names.size()));
		String n5race = races.get((int)(Math.random()*races.size()));
		String n5job = npc_jobs.get((int)(Math.random()*npc_jobs.size()));
		String qu1 = quests.get((int)(Math.random()*quests.size()));
		String qu2 = quests.get((int)(Math.random()*quests.size()));
		String qu3 = quests.get((int)(Math.random()*quests.size()));
		String qu4 = quests.get((int)(Math.random()*quests.size()));
		String qu5 = quests.get((int)(Math.random()*quests.size()));
		String qu6 = quests.get((int)(Math.random()*quests.size()));
		String qu7 = quests.get((int)(Math.random()*quests.size()));
		String qu8 = quests.get((int)(Math.random()*quests.size()));
		String qu9 = quests.get((int)(Math.random()*quests.size()));
		String qu10 = quests.get((int)(Math.random()*quests.size()));
		return new Town(this, x*20, y*20,
								name, pred_race, not_building,
								t1name, t1type, t1size, t1owner, t1race,
								t2name, t2type, t2size, t2owner, t2race,
								t3name, t3type, t3size, t3owner, t3race,
								t4name, t4type, t4size, t4owner, t4race,
								rulername,
								n1name, n1race, n1job,
								n2name, n2race, n2job,
								n3name, n3race, n3job,
								n4name, n4race, n4job,
								n5name, n5race, n5job,
								qu1, qu2, qu3, qu4, qu5, qu6, qu7, qu8, qu9, qu10, "false", "true");
	}

	private Camp createNewCamp(int x, int y)
	{
		String name = camps.get((int)(Math.random()*camps.size()));
		String pred_race;
		if(name.toLowerCase().contains("farm"))
			pred_race = farm_races.get((int)(Math.random()*farm_races.size()));
		else if(name.toLowerCase().contains("mine"))
			pred_race = mine_races.get((int)(Math.random()*mine_races.size()));
		else if(name.toLowerCase().contains("temple"))
		{
			int race_num = (int)(Math.random()*temple_deities.size());
			name += " of " + temple_deities.get(race_num);
			pred_race = deity_races.get(race_num).get((int)(Math.random()*deity_races.get(race_num).size()));
//			if(name.toLowerCase().contains("avandra"))
//				pred_race = avandra_races.get((int)(Math.random()*avandra_races.size()));
//			else if(name.toLowerCase().contains("bahamut"))
//				pred_race = bahamut_races.get((int)(Math.random()*bahamut_races.size()));
//			else if(name.toLowerCase().contains("corellon"))
//				pred_race = corellon_races.get((int)(Math.random()*corellon_races.size()));
//			else if(name.toLowerCase().contains("erathis"))
//				pred_race = erathis_races.get((int)(Math.random()*erathis_races.size()));
//			else if(name.toLowerCase().contains("ioun"))
//				pred_race = ioun_races.get((int)(Math.random()*ioun_races.size()));
//			else if(name.toLowerCase().contains("kord"))
//				pred_race = kord_races.get((int)(Math.random()*kord_races.size()));
//			else if(name.toLowerCase().contains("melora"))
//				pred_race = melora_races.get((int)(Math.random()*melora_races.size()));
//			else if(name.toLowerCase().contains("moradin"))
//				pred_race = moradin_races.get((int)(Math.random()*moradin_races.size()));
//			else if(name.toLowerCase().contains("pelor"))
//				pred_race = pelor_races.get((int)(Math.random()*pelor_races.size()));
//			else if(name.toLowerCase().contains("raven"))
//				pred_race = raven_queen_races.get((int)(Math.random()*raven_queen_races.size()));
//			else if(name.toLowerCase().contains("sehanine"))
//				pred_race = sehanine_races.get((int)(Math.random()*sehanine_races.size()));
//			else
//				pred_race = "Human";
		}
		else if(name.toLowerCase().contains("shrine"))
		{
			int race_num = (int)(Math.random()*deities.size());
			name += " to " + deities.get(race_num);
			pred_race = deity_races.get(race_num).get((int)(Math.random()*deity_races.get(race_num).size()));
//			if(name.toLowerCase().contains("avandra"))
//				pred_race = avandra_races.get((int)(Math.random()*avandra_races.size()));
//			else if(name.toLowerCase().contains("bahamut"))
//				pred_race = bahamut_races.get((int)(Math.random()*bahamut_races.size()));
//			else if(name.toLowerCase().contains("corellon"))
//				pred_race = corellon_races.get((int)(Math.random()*corellon_races.size()));
//			else if(name.toLowerCase().contains("erathis"))
//				pred_race = erathis_races.get((int)(Math.random()*erathis_races.size()));
//			else if(name.toLowerCase().contains("ioun"))
//				pred_race = ioun_races.get((int)(Math.random()*ioun_races.size()));
//			else if(name.toLowerCase().contains("kord"))
//				pred_race = kord_races.get((int)(Math.random()*kord_races.size()));
//			else if(name.toLowerCase().contains("melora"))
//				pred_race = melora_races.get((int)(Math.random()*melora_races.size()));
//			else if(name.toLowerCase().contains("moradin"))
//				pred_race = moradin_races.get((int)(Math.random()*moradin_races.size()));
//			else if(name.toLowerCase().contains("pelor"))
//				pred_race = pelor_races.get((int)(Math.random()*pelor_races.size()));
//			else if(name.toLowerCase().contains("raven"))
//				pred_race = raven_queen_races.get((int)(Math.random()*raven_queen_races.size()));
//			else if(name.toLowerCase().contains("sehanine"))
//				pred_race = sehanine_races.get((int)(Math.random()*sehanine_races.size()));
//			else if(name.toLowerCase().contains("asmodeus"))
//				pred_race = asmodeus_races.get((int)(Math.random()*asmodeus_races.size()));
//			else if(name.toLowerCase().contains("bane"))
//				pred_race = bane_races.get((int)(Math.random()*bane_races.size()));
//			else if(name.toLowerCase().contains("gruumsh"))
//				pred_race = gruumsh_races.get((int)(Math.random()*gruumsh_races.size()));
//			else if(name.toLowerCase().contains("lolth"))
//				pred_race = lolth_races.get((int)(Math.random()*lolth_races.size()));
//			else if(name.toLowerCase().contains("tiamat"))
//				pred_race = tiamat_races.get((int)(Math.random()*tiamat_races.size()));
//			else if(name.toLowerCase().contains("torog"))
//				pred_race = torog_races.get((int)(Math.random()*torog_races.size()));
//			else if(name.toLowerCase().contains("vecna"))
//				pred_race = vecna_races.get((int)(Math.random()*vecna_races.size()));
//			else if(name.toLowerCase().contains("zehir"))
//				pred_race = zehir_races.get((int)(Math.random()*zehir_races.size()));
//			else
//				pred_race = "Human";
		}
		else if(name.toLowerCase().contains("monastery"))
			pred_race = monastery_races.get((int)(Math.random()*monastery_races.size()));
		else
			pred_race = races.get((int)(Math.random()*races.size()));
		String not_building = "";
		String t1name = "";
		String t1type =  "";
		String t1size =  "";
		String t1owner =  "";
		String t1race =  "";
		String t2name = "";
		String t2type =  "";
		String t2size =  "";
		String t2owner =  "";
		String t2race =  "";
		String t3name =  "";
		String t3type =  "";
		String t3size =  "";
		String t3owner =  "";
		String t3race =  "";
		String t4name =  "";
		String t4type =  "";
		String t4size =  "";
		String t4owner =  "";
		String t4race =  "";
		String rulername =  "";
		String n1name = npc_names.get((int)(Math.random()*npc_names.size()));
		String n1race = pred_race;
		String n1job = npc_jobs.get((int)(Math.random()*npc_jobs.size()));
		String n2name = npc_names.get((int)(Math.random()*npc_names.size()));
		String n2race = pred_race;
		String n2job = npc_jobs.get((int)(Math.random()*npc_jobs.size()));
		String n3name = npc_names.get((int)(Math.random()*npc_names.size()));
		String n3race = pred_race;
		String n3job = npc_jobs.get((int)(Math.random()*npc_jobs.size()));
		String n4name = npc_names.get((int)(Math.random()*npc_names.size()));
		String n4race = pred_race;
		String n4job = npc_jobs.get((int)(Math.random()*npc_jobs.size()));
		String n5name = npc_names.get((int)(Math.random()*npc_names.size()));
		String n5race = pred_race;
		String n5job = npc_jobs.get((int)(Math.random()*npc_jobs.size()));
		if(name.toLowerCase().contains("temple"))
		{
			int j = (int)(Math.random()*3+1);
			if(j == 1)
				n1job = "Cleric";
			else
				n1job = "Priest";
			j = (int)(Math.random()*3+1);
			if(j == 1)
				n2job = "Cleric";
			else
				n2job = "Priest";
			j = (int)(Math.random()*3+1);
			if(j == 1)
				n3job = "Cleric";
			else
				n3job = "Priest";
			j = (int)(Math.random()*3+1);
			if(j == 1)
				n4job = "Cleric";
			else
				n4job = "Priest";
			j = (int)(Math.random()*3+1);
			if(j == 1)
				n5job = "Cleric";
			else
				n5job = "Priest";
		}
		else if(name.toLowerCase().contains("monastery"))
		{
			n1job = "Monk";
			n2job = "Monk";
			n3job = "Monk";
			n4job = "Monk";
			n5job = "Monk";
		}
		else if(name.toLowerCase().contains("shrine"))
		{
			n1job = "Follower";
			n2job = "Follower";
			n3job = "Follower";
			n4job = "Follower";
			n5job = "Follower";
		}
		String qu1 = quests.get((int)(Math.random()*quests.size()));
		String qu2 = quests.get((int)(Math.random()*quests.size()));
		String qu3 = quests.get((int)(Math.random()*quests.size()));
		String qu4 = quests.get((int)(Math.random()*quests.size()));
		String qu5 = quests.get((int)(Math.random()*quests.size()));
		String qu6 = quests.get((int)(Math.random()*quests.size()));
		String qu7 = quests.get((int)(Math.random()*quests.size()));
		String qu8 = quests.get((int)(Math.random()*quests.size()));
		String qu9 = quests.get((int)(Math.random()*quests.size()));
		String qu10 = quests.get((int)(Math.random()*quests.size()));
		return new Camp(this, x, y,
								name, pred_race, not_building,
								t1name, t1type, t1size, t1owner, t1race,
								t2name, t2type, t2size, t2owner, t2race,
								t3name, t3type, t3size, t3owner, t3race,
								t4name, t4type, t4size, t4owner, t4race,
								rulername,
								n1name, n1race, n1job,
								n2name, n2race, n2job,
								n3name, n3race, n3job,
								n4name, n4race, n4job,
								n5name, n5race, n5job,
								qu1, qu2, qu3, qu4, qu5, qu6, qu7, qu8, qu9, qu10, "true", "true");
	}

	private Landmark createNewLandmark(int x, int y)
	{
		String name = landmark_names.get((int)(Math.random()*landmark_names.size()));
		if(name.toLowerCase().contains("temple"))
			name += " of " + temple_deities.get((int)(Math.random()*temple_deities.size()));
		else if(name.toLowerCase().contains("shrine"))
			name += " to " + deities.get((int)(Math.random()*deities.size()));
		return new Landmark(this, x*20, y*20, name, "true", "true");
	}

	private void readLists()
	{
		BufferedReader city_reader = getReader(Constants.DATA_FOLDER + "\\" + "citynames.txt");
		try
		{
			line = city_reader.readLine();
		}
		catch(IOException e)
		{
			System.exit(0);
		}

		while(line != null)
		{
			city_names.add(line);
			try
			{
				line = city_reader.readLine();
			}
			catch(IOException e)
			{
				System.exit(0);
			}
		}

		BufferedReader fortress_reader = getReader(Constants.DATA_FOLDER + "\\" + "fortressnames.txt");
		try
		{
			line = fortress_reader.readLine();
		}
		catch(IOException e)
		{
			System.exit(0);
		}

		while(line != null)
		{
			fortress_names.add(line);
			try
			{
				line = fortress_reader.readLine();
			}
			catch(IOException e)
			{
				System.exit(0);
			}
		}

		BufferedReader town_reader = getReader(Constants.DATA_FOLDER + "\\" + "townnames.txt");
		try
		{
			line = town_reader.readLine();
		}
		catch(IOException e)
		{
			System.exit(0);
		}

		while(line != null)
		{
			town_names.add(line);
			try
			{
				line = town_reader.readLine();
			}
			catch(IOException e)
			{
				System.exit(0);
			}
		}

		BufferedReader npc_name_reader = getReader(Constants.DATA_FOLDER + "\\npcnames.txt");
		try
		{
			line = npc_name_reader.readLine();
		}
		catch(IOException e)
		{
			System.exit(0);
		}

		while(line != null)
		{
			npc_names.add(line);
			try
			{
				line = npc_name_reader.readLine();
			}
			catch(IOException e)
			{
				System.exit(0);
			}
		}

		npc_name_reader = getReader(Constants.DATA_FOLDER + "\\rulertypes.txt");
		try
		{
			line = npc_name_reader.readLine();
		}
		catch(IOException e)
		{
			System.exit(0);
		}

		while(line != null)
		{
			ruler_types.add(line);
			try
			{
				line = npc_name_reader.readLine();
			}
			catch(IOException e)
			{
				System.exit(0);
			}
		}

		BufferedReader npc_job_reader = getReader(Constants.DATA_FOLDER + "\\" + "npcjobs.txt");
		try
		{
			line = npc_job_reader.readLine();
		}
		catch(IOException e)
		{
			System.exit(0);
		}

		while(line != null)
		{
			npc_jobs.add(line);
			try
			{
				line = npc_job_reader.readLine();
			}
			catch(IOException e)
			{
				System.exit(0);
			}
		}

		BufferedReader landmark_reader = getReader(Constants.DATA_FOLDER + "\\" + "landmarks.txt");
		try
		{
			line = landmark_reader.readLine();
		}
		catch(IOException e)
		{
			System.exit(0);
		}

		while(line != null)
		{
			landmark_names.add(line);
			try
			{
				line = landmark_reader.readLine();
			}
			catch(IOException e)
			{
				System.exit(0);
			}
		}

		BufferedReader quest_reader = getReader(Constants.DATA_FOLDER + "\\" + "quests.txt");
		try
		{
			line = quest_reader.readLine();
		}
		catch(IOException e)
		{
			System.exit(0);
		}

		while(line != null)
		{
			quests.add(line);
			try
			{
				line = quest_reader.readLine();
			}
			catch(IOException e)
			{
				System.exit(0);
			}
		}

		BufferedReader city_landmark_reader = getReader(Constants.DATA_FOLDER + "\\" + "citylandmarks.txt");
		try
		{
			line = city_landmark_reader.readLine();
		}
		catch(IOException e)
		{
			System.exit(0);
		}

		while(line != null)
		{
			city_landmarks.add(line);
			try
			{
				line = city_landmark_reader.readLine();
			}
			catch(IOException e)
			{
				System.exit(0);
			}
		}

		BufferedReader tavern_name_reader = getReader(Constants.DATA_FOLDER + "\\" + "tavernnames.txt");
		try
		{
			line = tavern_name_reader.readLine();
		}
		catch(IOException e)
		{
			System.exit(0);
		}

		while(line != null)
		{
			tavern_names.add(line);
			try
			{
				line = tavern_name_reader.readLine();
			}
			catch(IOException e)
			{
				System.exit(0);
			}
		}

		BufferedReader tavern_type_reader = getReader(Constants.DATA_FOLDER + "\\" + "taverntypes.txt");
		try
		{
			line = tavern_type_reader.readLine();
		}
		catch(IOException e)
		{
			System.exit(0);
		}

		while(line != null)
		{
			tavern_types.add(line);
			try
			{
				line = tavern_type_reader.readLine();
			}
			catch(IOException e)
			{
				System.exit(0);
			}
		}

		BufferedReader tavern_size_reader = getReader(Constants.DATA_FOLDER + "\\" + "tavernsizes.txt");
		try
		{
			line = tavern_size_reader.readLine();
		}
		catch(IOException e)
		{
			System.exit(0);
		}

		while(line != null)
		{
			tavern_sizes.add(line);
			try
			{
				line = tavern_size_reader.readLine();
			}
			catch(IOException e)
			{
				System.exit(0);
			}
		}

		BufferedReader camp_reader = getReader(Constants.DATA_FOLDER + "\\" + "camps.txt");
		try
		{
			line = camp_reader.readLine();
		}
		catch(IOException e)
		{
			System.exit(0);
		}

		while(line != null)
		{
			camps.add(line);
			try
			{
				line = camp_reader.readLine();
			}
			catch(IOException e)
			{
				System.exit(0);
			}
		}

		camp_reader = getReader(Constants.DATA_FOLDER + "\\" + "deities.txt");
		boolean reading_temple = true;
		try
		{
			line = camp_reader.readLine();
		}
		catch(IOException e)
		{
			System.exit(0);
		}

		while(line != null)
		{
			if(!line.equals("//temple deities//"))
			{
				if(line.equals("//end of temple deities//"))
				{
					reading_temple = false;
				}
				else if(reading_temple)
				{
					temple_deities.add(line);
					deities.add(line);
				}
				else if(!reading_temple)
				{
					deities.add(line);
				}
			}
			try
			{
				line = camp_reader.readLine();
			}
			catch(IOException e)
			{
				System.exit(0);
			}
		}

//		camp_reader = getReader(Constants.DATA_FOLDER + "\\" + "templedeities.txt");
//		try
//		{
//			line = camp_reader.readLine();
//		}
//		catch(IOException e)
//		{
//			System.exit(0);
//		}
//
//		while(line != null)
//		{
//			temple_deities.add(line);
//			try
//			{
//				line = camp_reader.readLine();
//			}
//			catch(IOException e)
//			{
//				System.exit(0);
//			}
//		}

		BufferedReader race_reader = getReader(Constants.DATA_FOLDER + "\\" + "races.txt");
		try
		{
			line = race_reader.readLine();
		}
		catch(IOException e)
		{
			System.exit(0);
		}

		while(line != null)
		{
			races.add(line);
			try
			{
				line = race_reader.readLine();
			}
			catch(IOException e)
			{
				System.exit(0);
			}
		}

		race_reader = getReader(Constants.DATA_FOLDER + "\\" + "races.txt");
		try
		{
			line = race_reader.readLine();
		}
		catch(IOException e)
		{
			System.exit(0);
		}

		while(line != null)
		{
			races.add(line);
			try
			{
				line = race_reader.readLine();
			}
			catch(IOException e)
			{
				System.exit(0);
			}
		}

		race_reader = getReader(Constants.DATA_FOLDER + "\\" + "cityracescommon.txt");
		try
		{
			line = race_reader.readLine();
		}
		catch(IOException e)
		{
			System.exit(0);
		}

		while(line != null)
		{
			city_races_common.add(line);
			try
			{
				line = race_reader.readLine();
			}
			catch(IOException e)
			{
				System.exit(0);
			}
		}

		race_reader = getReader(Constants.DATA_FOLDER + "\\" + "cityracesuncommon.txt");
		try
		{
			line = race_reader.readLine();
		}
		catch(IOException e)
		{
			System.exit(0);
		}

		while(line != null)
		{
			city_races_uncommon.add(line);
			try
			{
				line = race_reader.readLine();
			}
			catch(IOException e)
			{
				System.exit(0);
			}
		}

		race_reader = getReader(Constants.DATA_FOLDER + "\\" + "fortressracescommon.txt");
		try
		{
			line = race_reader.readLine();
		}
		catch(IOException e)
		{
			System.exit(0);
		}

		while(line != null)
		{
			fortress_races_common.add(line);
			try
			{
				line = race_reader.readLine();
			}
			catch(IOException e)
			{
				System.exit(0);
			}
		}

		race_reader = getReader(Constants.DATA_FOLDER + "\\" + "fortressracesuncommon.txt");
		try
		{
			line = race_reader.readLine();
		}
		catch(IOException e)
		{
			System.exit(0);
		}

		while(line != null)
		{
			fortress_races_uncommon.add(line);
			try
			{
				line = race_reader.readLine();
			}
			catch(IOException e)
			{
				System.exit(0);
			}
		}

		race_reader = getReader(Constants.DATA_FOLDER + "\\" + "townracescommon.txt");
		try
		{
			line = race_reader.readLine();
		}
		catch(IOException e)
		{
			System.exit(0);
		}

		while(line != null)
		{
			town_races_common.add(line);
			try
			{
				line = race_reader.readLine();
			}
			catch(IOException e)
			{
				System.exit(0);
			}
		}

		race_reader = getReader(Constants.DATA_FOLDER + "\\" + "townracesuncommon.txt");
		try
		{
			line = race_reader.readLine();
		}
		catch(IOException e)
		{
			System.exit(0);
		}

		while(line != null)
		{
			town_races_uncommon.add(line);
			try
			{
				line = race_reader.readLine();
			}
			catch(IOException e)
			{
				System.exit(0);
			}
		}

		race_reader = getReader(Constants.DATA_FOLDER + "\\" + "farmraces.txt");
		try
		{
			line = race_reader.readLine();
		}
		catch(IOException e)
		{
			System.exit(0);
		}

		while(line != null)
		{
			farm_races.add(line);
			try
			{
				line = race_reader.readLine();
			}
			catch(IOException e)
			{
				System.exit(0);
			}
		}

		race_reader = getReader(Constants.DATA_FOLDER + "\\" + "mineraces.txt");
		try
		{
			line = race_reader.readLine();
		}
		catch(IOException e)
		{
			System.exit(0);
		}

		while(line != null)
		{
			mine_races.add(line);
			try
			{
				line = race_reader.readLine();
			}
			catch(IOException e)
			{
				System.exit(0);
			}
		}

		race_reader = getReader(Constants.DATA_FOLDER + "\\" + "templeraces.txt");
		try
		{
			line = race_reader.readLine();
		}
		catch(IOException e)
		{
			System.exit(0);
		}

		while(line != null)
		{
			temple_races.add(line);
			try
			{
				line = race_reader.readLine();
			}
			catch(IOException e)
			{
				System.exit(0);
			}
		}


		for(int i = 0; i < deities.size(); i++)
		{
//			try
//			{
				race_reader = getReader(Constants.DATA_FOLDER + "\\" + deities.get(i) + "races.txt");
//			}
//			catch(FileNotFoundException e)
//			{
//				break;
//			}
			deity_races.add(new ArrayList<String>());
			try
			{
				line = race_reader.readLine();
//				System.out.println(line);
			}
			catch(IOException e)
			{
				System.exit(0);
			}

			while(line != null)
			{
				deity_races.get(i).add(line);
				try
				{
					line = race_reader.readLine();
//					System.out.println(line);
				}
				catch(IOException e)
				{
					System.exit(0);
				}
			}
		}

//		race_reader = getReader(Constants.DATA_FOLDER + "\\" + "avandraraces.txt");
//		try
//		{
//			line = race_reader.readLine();
//		}
//		catch(IOException e)
//		{
//			System.exit(0);
//		}
//
//		while(line != null)
//		{
//			avandra_races.add(line);
//			try
//			{
//				line = race_reader.readLine();
//			}
//			catch(IOException e)
//			{
//				System.exit(0);
//			}
//		}
//
//		race_reader = getReader(Constants.DATA_FOLDER + "\\" + "bahamutraces.txt");
//		try
//		{
//			line = race_reader.readLine();
//		}
//		catch(IOException e)
//		{
//			System.exit(0);
//		}
//
//		while(line != null)
//		{
//			bahamut_races.add(line);
//			try
//			{
//				line = race_reader.readLine();
//			}
//			catch(IOException e)
//			{
//				System.exit(0);
//			}
//		}
//
//		race_reader = getReader(Constants.DATA_FOLDER + "\\" + "corellonraces.txt");
//		try
//		{
//			line = race_reader.readLine();
//		}
//		catch(IOException e)
//		{
//			System.exit(0);
//		}
//
//		while(line != null)
//		{
//			corellon_races.add(line);
//			try
//			{
//				line = race_reader.readLine();
//			}
//			catch(IOException e)
//			{
//				System.exit(0);
//			}
//		}
//
//		race_reader = getReader(Constants.DATA_FOLDER + "\\" + "erathisraces.txt");
//		try
//		{
//			line = race_reader.readLine();
//		}
//		catch(IOException e)
//		{
//			System.exit(0);
//		}
//
//		while(line != null)
//		{
//			erathis_races.add(line);
//			try
//			{
//				line = race_reader.readLine();
//			}
//			catch(IOException e)
//			{
//				System.exit(0);
//			}
//		}
//
//		race_reader = getReader(Constants.DATA_FOLDER + "\\" + "iounraces.txt");
//		try
//		{
//			line = race_reader.readLine();
//		}
//		catch(IOException e)
//		{
//			System.exit(0);
//		}
//
//		while(line != null)
//		{
//			ioun_races.add(line);
//			try
//			{
//				line = race_reader.readLine();
//			}
//			catch(IOException e)
//			{
//				System.exit(0);
//			}
//		}
//
//		race_reader = getReader(Constants.DATA_FOLDER + "\\" + "kordraces.txt");
//		try
//		{
//			line = race_reader.readLine();
//		}
//		catch(IOException e)
//		{
//			System.exit(0);
//		}
//
//		while(line != null)
//		{
//			kord_races.add(line);
//			try
//			{
//				line = race_reader.readLine();
//			}
//			catch(IOException e)
//			{
//				System.exit(0);
//			}
//		}
//
//		race_reader = getReader(Constants.DATA_FOLDER + "\\" + "meloraraces.txt");
//		try
//		{
//			line = race_reader.readLine();
//		}
//		catch(IOException e)
//		{
//			System.exit(0);
//		}
//
//		while(line != null)
//		{
//			melora_races.add(line);
//			try
//			{
//				line = race_reader.readLine();
//			}
//			catch(IOException e)
//			{
//				System.exit(0);
//			}
//		}
//
//		race_reader = getReader(Constants.DATA_FOLDER + "\\" + "moradinraces.txt");
//		try
//		{
//			line = race_reader.readLine();
//		}
//		catch(IOException e)
//		{
//			System.exit(0);
//		}
//
//		while(line != null)
//		{
//			moradin_races.add(line);
//			try
//			{
//				line = race_reader.readLine();
//			}
//			catch(IOException e)
//			{
//				System.exit(0);
//			}
//		}
//
//		race_reader = getReader(Constants.DATA_FOLDER + "\\" + "pelorraces.txt");
//		try
//		{
//			line = race_reader.readLine();
//		}
//		catch(IOException e)
//		{
//			System.exit(0);
//		}
//
//		while(line != null)
//		{
//			pelor_races.add(line);
//			try
//			{
//				line = race_reader.readLine();
//			}
//			catch(IOException e)
//			{
//				System.exit(0);
//			}
//		}
//
//		race_reader = getReader(Constants.DATA_FOLDER + "\\" + "the raven queenraces.txt");
//		try
//		{
//			line = race_reader.readLine();
//		}
//		catch(IOException e)
//		{
//			System.exit(0);
//		}
//
//		while(line != null)
//		{
//			raven_queen_races.add(line);
//			try
//			{
//				line = race_reader.readLine();
//			}
//			catch(IOException e)
//			{
//				System.exit(0);
//			}
//		}
//
//		race_reader = getReader(Constants.DATA_FOLDER + "\\" + "sehanineraces.txt");
//		try
//		{
//			line = race_reader.readLine();
//		}
//		catch(IOException e)
//		{
//			System.exit(0);
//		}
//
//		while(line != null)
//		{
//			sehanine_races.add(line);
//			try
//			{
//				line = race_reader.readLine();
//			}
//			catch(IOException e)
//			{
//				System.exit(0);
//			}
//		}
//
//		race_reader = getReader(Constants.DATA_FOLDER + "\\" + "asmodeusraces.txt");
//		try
//		{
//			line = race_reader.readLine();
//		}
//		catch(IOException e)
//		{
//			System.exit(0);
//		}
//
//		while(line != null)
//		{
//			asmodeus_races.add(line);
//			try
//			{
//				line = race_reader.readLine();
//			}
//			catch(IOException e)
//			{
//				System.exit(0);
//			}
//		}
//
//		race_reader = getReader(Constants.DATA_FOLDER + "\\" + "baneraces.txt");
//		try
//		{
//			line = race_reader.readLine();
//		}
//		catch(IOException e)
//		{
//			System.exit(0);
//		}
//
//		while(line != null)
//		{
//			bane_races.add(line);
//			try
//			{
//				line = race_reader.readLine();
//			}
//			catch(IOException e)
//			{
//				System.exit(0);
//			}
//		}
//
//		race_reader = getReader(Constants.DATA_FOLDER + "\\" + "gruumshraces.txt");
//		try
//		{
//			line = race_reader.readLine();
//		}
//		catch(IOException e)
//		{
//			System.exit(0);
//		}
//
//		while(line != null)
//		{
//			gruumsh_races.add(line);
//			try
//			{
//				line = race_reader.readLine();
//			}
//			catch(IOException e)
//			{
//				System.exit(0);
//			}
//		}
//
//		race_reader = getReader(Constants.DATA_FOLDER + "\\" + "lolthraces.txt");
//		try
//		{
//			line = race_reader.readLine();
//		}
//		catch(IOException e)
//		{
//			System.exit(0);
//		}
//
//		while(line != null)
//		{
//			lolth_races.add(line);
//			try
//			{
//				line = race_reader.readLine();
//			}
//			catch(IOException e)
//			{
//				System.exit(0);
//			}
//		}
//
//		race_reader = getReader(Constants.DATA_FOLDER + "\\" + "tiamatraces.txt");
//		try
//		{
//			line = race_reader.readLine();
//		}
//		catch(IOException e)
//		{
//			System.exit(0);
//		}
//
//		while(line != null)
//		{
//			tiamat_races.add(line);
//			try
//			{
//				line = race_reader.readLine();
//			}
//			catch(IOException e)
//			{
//				System.exit(0);
//			}
//		}
//
//		race_reader = getReader(Constants.DATA_FOLDER + "\\" + "torograces.txt");
//		try
//		{
//			line = race_reader.readLine();
//		}
//		catch(IOException e)
//		{
//			System.exit(0);
//		}
//
//		while(line != null)
//		{
//			torog_races.add(line);
//			try
//			{
//				line = race_reader.readLine();
//			}
//			catch(IOException e)
//			{
//				System.exit(0);
//			}
//		}
//
//		race_reader = getReader(Constants.DATA_FOLDER + "\\" + "vecnaraces.txt");
//		try
//		{
//			line = race_reader.readLine();
//		}
//		catch(IOException e)
//		{
//			System.exit(0);
//		}
//
//		while(line != null)
//		{
//			vecna_races.add(line);
//			try
//			{
//				line = race_reader.readLine();
//			}
//			catch(IOException e)
//			{
//				System.exit(0);
//			}
//		}
//
//		race_reader = getReader(Constants.DATA_FOLDER + "\\" + "zehirraces.txt");
//		try
//		{
//			line = race_reader.readLine();
//		}
//		catch(IOException e)
//		{
//			System.exit(0);
//		}
//
//		while(line != null)
//		{
//			zehir_races.add(line);
//			try
//			{
//				line = race_reader.readLine();
//			}
//			catch(IOException e)
//			{
//				System.exit(0);
//			}
//		}

		race_reader = getReader(Constants.DATA_FOLDER + "\\" + "monasteryraces.txt");
		try
		{
			line = race_reader.readLine();
		}
		catch(IOException e)
		{
			System.exit(0);
		}

		while(line != null)
		{
			monastery_races.add(line);
			try
			{
				line = race_reader.readLine();
			}
			catch(IOException e)
			{
				System.exit(0);
			}
		}
	}

	private BufferedReader getReader(String name)
	{
		BufferedReader in = null;
		try
		{
			File file = new File(name);
			in = new BufferedReader(new FileReader(file));
		}
		catch(FileNotFoundException e)
		{
			System.exit(0);
		}
		catch(IOException e)
		{
			System.exit(0);
		}
		return in;
	}

	private PrintWriter openWriter(String name)
	{
		try
		{
			File file = new File(name);
			out = new PrintWriter(new BufferedWriter(new FileWriter(file)), true);
			return out;
		}
		catch(IOException e)
		{
			System.out.println("I/O Error");
			System.exit(0);
		}
		return null;
	}

	public boolean isEditing() {return edit_mode;}

	public void actionPerformed(ActionEvent event)
	{
		for(Map_Object r : actors)
			r.act();
	}

	public void clearDrawGrid()
	{
		for(int i = 0; i < draw_grid.length; i++)
			for(int j = 0; j < draw_grid[i].length; j++)
				draw_grid[i][j] = null;
	}

	public void addToDraw(HUD_Object h, int i, int j)
	{
		draw_grid[i][j] = h;
	}

	public void addMultipleDraw(int x1, int y1, int x2, int y2, String type)
	{
		clearDrawGrid();
		String selected = selected_terrain;
		if(!selected.equals(""))
		{
			if(type.equals("Rectangle"))
			{
				for(int i = x1; i <= x2; i++)
					for(int j = y1; j <= y2; j++)
					{
						HUD_Object h = null;
						if(selected.equals(Grass.CODE))
							h = new HUD_Grass(this, i*20, j*20);
						else if(selected.equals(Dirt.CODE))
							h = new HUD_Dirt(this, i*20, j*20);
						else if(selected.equals(Water.CODE))
							h = new HUD_Water(this, i*20, j*20);
						else if(selected.equals(Sand.CODE))
							h = new HUD_Sand(this, i*20, j*20);
						else if(selected.equals(Snow.CODE))
							h = new HUD_Snow(this, i*20, j*20);
						else if(selected.equals(Mountain.CODE))
							h = new HUD_Mountain(this, i*20, j*20);
						else if(selected.equals(Tree.CODE))
							h = new HUD_Tree(this, i*20, j*20);
						else if(selected.equals(City.CODE))
							h = new HUD_City(this, i*20, j*20);
						else if(selected.equals(Fortress.CODE))
							h = new HUD_Fortress(this, i*20, j*20);
						else if(selected.equals(Town.CODE))
							h = new HUD_Town(this, i*20, j*20);
						else if(selected.equals(Camp.CODE))
							h = new HUD_Camp(this, i*20, j*20);
						else if(selected.equals(Landmark.CODE))
							h = new HUD_Landmark(this, i*20, j*20);
						addToDraw(h, i, j);
					}
			}
			else
			{
				int a = (x2-x1)/2;
				int b = (y2-y1)/2;
				int centrex = x1+a;
				int centrey = y1+b;
				for(int i = 0; i < a; i++)
				{
					//bottom right
					int locx = centrex+i;
					int locy = centrey+(int)Math.round(b*Math.sqrt(1 - Math.pow(i, 2)/a/a));
					for(int j = 0; locy>centrey; j++)
					{
						locy = centrey+(int)Math.round(b*Math.sqrt(1 - Math.pow(i, 2)/a/a))-j;
						HUD_Object h = null;
						if(selected.equals(Grass.CODE))
							h = new HUD_Grass(this, locx*20, locy*20);
						else if(selected.equals(Dirt.CODE))
							h = new HUD_Dirt(this, locx*20, locy*20);
						else if(selected.equals(Water.CODE))
							h = new HUD_Water(this, locx*20, locy*20);
						else if(selected.equals(Sand.CODE))
							h = new HUD_Sand(this, locx*20, locy*20);
						else if(selected.equals(Snow.CODE))
							h = new HUD_Snow(this, locx*20, locy*20);
						else if(selected.equals(Mountain.CODE))
							h = new HUD_Mountain(this, locx*20, locy*20);
						else if(selected.equals(Tree.CODE))
							h = new HUD_Tree(this, locx*20, locy*20);
						else if(selected.equals(City.CODE))
							h = new HUD_City(this, locx*20, locy*20);
						else if(selected.equals(Fortress.CODE))
							h = new HUD_Fortress(this, locx*20, locy*20);
						else if(selected.equals(Town.CODE))
							h = new HUD_Town(this, locx*20, locy*20);
						else if(selected.equals(Camp.CODE))
							h = new HUD_Camp(this, locx*20, locy*20);
						else if(selected.equals(Landmark.CODE))
							h = new HUD_Landmark(this, locx*20, locy*20);
						addToDraw(h, locx, locy);
		//				terrain_grid[locx][locy] = new Dirt(this, locx*20, locy*20);
					}

					//bottom left
					locx = centrex-i;
					locy = centrey+(int)Math.round(b*Math.sqrt(1 - Math.pow(i, 2)/a/a));
					for(int j = 0; locy>centrey; j++)
					{
						locy = centrey+(int)Math.round(b*Math.sqrt(1 - Math.pow(i, 2)/a/a))-j;
						HUD_Object h = null;
						if(selected.equals(Grass.CODE))
							h = new HUD_Grass(this, locx*20, locy*20);
						else if(selected.equals(Dirt.CODE))
							h = new HUD_Dirt(this, locx*20, locy*20);
						else if(selected.equals(Water.CODE))
							h = new HUD_Water(this, locx*20, locy*20);
						else if(selected.equals(Sand.CODE))
							h = new HUD_Sand(this, locx*20, locy*20);
						else if(selected.equals(Snow.CODE))
							h = new HUD_Snow(this, locx*20, locy*20);
						else if(selected.equals(Mountain.CODE))
							h = new HUD_Mountain(this, locx*20, locy*20);
						else if(selected.equals(Tree.CODE))
							h = new HUD_Tree(this, locx*20, locy*20);
						else if(selected.equals(City.CODE))
							h = new HUD_City(this, locx*20, locy*20);
						else if(selected.equals(Fortress.CODE))
							h = new HUD_Fortress(this, locx*20, locy*20);
						else if(selected.equals(Town.CODE))
							h = new HUD_Town(this, locx*20, locy*20);
						else if(selected.equals(Camp.CODE))
							h = new HUD_Camp(this, locx*20, locy*20);
						else if(selected.equals(Landmark.CODE))
							h = new HUD_Landmark(this, locx*20, locy*20);
						addToDraw(h, locx, locy);
		//				terrain_grid[locx][locy] = new Dirt(this, locx*20, locy*20);
					}

					//top left
					locx = centrex-i;
					locy=centrey-(int)Math.round(b*Math.sqrt(1- Math.pow(i, 2)/a/a));
					for(int j = 0; locy<centrey; j++)
					{
						locy=centrey-(int)Math.round(b*Math.sqrt(1- Math.pow(i, 2)/a/a))+j;
						HUD_Object h = null;
						if(selected.equals(Grass.CODE))
							h = new HUD_Grass(this, locx*20, locy*20);
						else if(selected.equals(Dirt.CODE))
							h = new HUD_Dirt(this, locx*20, locy*20);
						else if(selected.equals(Water.CODE))
							h = new HUD_Water(this, locx*20, locy*20);
						else if(selected.equals(Sand.CODE))
							h = new HUD_Sand(this, locx*20, locy*20);
						else if(selected.equals(Snow.CODE))
							h = new HUD_Snow(this, locx*20, locy*20);
						else if(selected.equals(Mountain.CODE))
							h = new HUD_Mountain(this, locx*20, locy*20);
						else if(selected.equals(Tree.CODE))
							h = new HUD_Tree(this, locx*20, locy*20);
						else if(selected.equals(City.CODE))
							h = new HUD_City(this, locx*20, locy*20);
						else if(selected.equals(Fortress.CODE))
							h = new HUD_Fortress(this, locx*20, locy*20);
						else if(selected.equals(Town.CODE))
							h = new HUD_Town(this, locx*20, locy*20);
						else if(selected.equals(Camp.CODE))
							h = new HUD_Camp(this, locx*20, locy*20);
						else if(selected.equals(Landmark.CODE))
							h = new HUD_Landmark(this, locx*20, locy*20);
						addToDraw(h, locx, locy);
		//				terrain_grid[locx][locy] = new Dirt(this, locx*20, locy*20);
					}

					//top right
					locx = centrex+i;
					locy = centrey-(int)Math.round(b*Math.sqrt(1 - Math.pow(i, 2)/a/a));
					for(int j = 0; locy<centrey; j++)
					{
						locy = centrey-(int)Math.round(b*Math.sqrt(1 - Math.pow(i, 2)/a/a))+j;
						HUD_Object h = null;
						if(selected.equals(Grass.CODE))
							h = new HUD_Grass(this, locx*20, locy*20);
						else if(selected.equals(Dirt.CODE))
							h = new HUD_Dirt(this, locx*20, locy*20);
						else if(selected.equals(Water.CODE))
							h = new HUD_Water(this, locx*20, locy*20);
						else if(selected.equals(Sand.CODE))
							h = new HUD_Sand(this, locx*20, locy*20);
						else if(selected.equals(Snow.CODE))
							h = new HUD_Snow(this, locx*20, locy*20);
						else if(selected.equals(Mountain.CODE))
							h = new HUD_Mountain(this, locx*20, locy*20);
						else if(selected.equals(Tree.CODE))
							h = new HUD_Tree(this, locx*20, locy*20);
						else if(selected.equals(City.CODE))
							h = new HUD_City(this, locx*20, locy*20);
						else if(selected.equals(Fortress.CODE))
							h = new HUD_Fortress(this, locx*20, locy*20);
						else if(selected.equals(Town.CODE))
							h = new HUD_Town(this, locx*20, locy*20);
						else if(selected.equals(Camp.CODE))
							h = new HUD_Camp(this, locx*20, locy*20);
						else if(selected.equals(Landmark.CODE))
							h = new HUD_Landmark(this, locx*20, locy*20);
						addToDraw(h, locx, locy);
		//				terrain_grid[locx][locy] = new Dirt(this, locx*20, locy*20);
					}
				}
				for(int i = 0; i < b; i++)
				{
					//bottom right
					int locy = centrey+i;
					int locx = centrex+(int)Math.round(a*Math.sqrt(1 - Math.pow(i, 2)/b/b));
					for(int j = 0; locx>centrex; j++)
					{
						locx = centrex+(int)Math.round(a*Math.sqrt(1 - Math.pow(i, 2)/b/b))-j;
						HUD_Object h = null;
						if(selected.equals(Grass.CODE))
							h = new HUD_Grass(this, locx*20, locy*20);
						else if(selected.equals(Dirt.CODE))
							h = new HUD_Dirt(this, locx*20, locy*20);
						else if(selected.equals(Water.CODE))
							h = new HUD_Water(this, locx*20, locy*20);
						else if(selected.equals(Sand.CODE))
							h = new HUD_Sand(this, locx*20, locy*20);
						else if(selected.equals(Snow.CODE))
							h = new HUD_Snow(this, locx*20, locy*20);
						else if(selected.equals(Mountain.CODE))
							h = new HUD_Mountain(this, locx*20, locy*20);
						else if(selected.equals(Tree.CODE))
							h = new HUD_Tree(this, locx*20, locy*20);
						else if(selected.equals(City.CODE))
							h = new HUD_City(this, locx*20, locy*20);
						else if(selected.equals(Fortress.CODE))
							h = new HUD_Fortress(this, locx*20, locy*20);
						else if(selected.equals(Town.CODE))
							h = new HUD_Town(this, locx*20, locy*20);
						else if(selected.equals(Camp.CODE))
							h = new HUD_Camp(this, locx*20, locy*20);
						else if(selected.equals(Landmark.CODE))
							h = new HUD_Landmark(this, locx*20, locy*20);
						addToDraw(h, locx, locy);
		//				terrain_grid[locx][locy] = new Dirt(this, locx*20, locy*20);
					}

					//bottom left
					locy = centrey+i;
					locx = centrex-(int)Math.round(a*Math.sqrt(1 - Math.pow(i, 2)/b/b));
					for(int j = 0; locx<centrex; j++)
					{
						locx = centrex-(int)Math.round(a*Math.sqrt(1 - Math.pow(i, 2)/b/b))+j;
						HUD_Object h = null;
						if(selected.equals(Grass.CODE))
							h = new HUD_Grass(this, locx*20, locy*20);
						else if(selected.equals(Dirt.CODE))
							h = new HUD_Dirt(this, locx*20, locy*20);
						else if(selected.equals(Water.CODE))
							h = new HUD_Water(this, locx*20, locy*20);
						else if(selected.equals(Sand.CODE))
							h = new HUD_Sand(this, locx*20, locy*20);
						else if(selected.equals(Snow.CODE))
							h = new HUD_Snow(this, locx*20, locy*20);
						else if(selected.equals(Mountain.CODE))
							h = new HUD_Mountain(this, locx*20, locy*20);
						else if(selected.equals(Tree.CODE))
							h = new HUD_Tree(this, locx*20, locy*20);
						else if(selected.equals(City.CODE))
							h = new HUD_City(this, locx*20, locy*20);
						else if(selected.equals(Fortress.CODE))
							h = new HUD_Fortress(this, locx*20, locy*20);
						else if(selected.equals(Town.CODE))
							h = new HUD_Town(this, locx*20, locy*20);
						else if(selected.equals(Camp.CODE))
							h = new HUD_Camp(this, locx*20, locy*20);
						else if(selected.equals(Landmark.CODE))
							h = new HUD_Landmark(this, locx*20, locy*20);
						addToDraw(h, locx, locy);
		//				terrain_grid[locx][locy] = new Dirt(this, locx*20, locy*20);
					}

					//top left
					locy = centrey-i;
					locx = centrex-(int)Math.round(a*Math.sqrt(1 - Math.pow(i, 2)/b/b));
					for(int j = 0; locx<centrex; j++)
					{
						locx = centrex-(int)Math.round(a*Math.sqrt(1 - Math.pow(i, 2)/b/b))+j;
						HUD_Object h = null;
						if(selected.equals(Grass.CODE))
							h = new HUD_Grass(this, locx*20, locy*20);
						else if(selected.equals(Dirt.CODE))
							h = new HUD_Dirt(this, locx*20, locy*20);
						else if(selected.equals(Water.CODE))
							h = new HUD_Water(this, locx*20, locy*20);
						else if(selected.equals(Sand.CODE))
							h = new HUD_Sand(this, locx*20, locy*20);
						else if(selected.equals(Snow.CODE))
							h = new HUD_Snow(this, locx*20, locy*20);
						else if(selected.equals(Mountain.CODE))
							h = new HUD_Mountain(this, locx*20, locy*20);
						else if(selected.equals(Tree.CODE))
							h = new HUD_Tree(this, locx*20, locy*20);
						else if(selected.equals(City.CODE))
							h = new HUD_City(this, locx*20, locy*20);
						else if(selected.equals(Fortress.CODE))
							h = new HUD_Fortress(this, locx*20, locy*20);
						else if(selected.equals(Town.CODE))
							h = new HUD_Town(this, locx*20, locy*20);
						else if(selected.equals(Camp.CODE))
							h = new HUD_Camp(this, locx*20, locy*20);
						else if(selected.equals(Landmark.CODE))
							h = new HUD_Landmark(this, locx*20, locy*20);
						addToDraw(h, locx, locy);
		//				terrain_grid[locx][locy] = new Dirt(this, locx*20, locy*20);
					}

					//top right
					locy = centrey-i;
					locx = centrex+(int)Math.round(a*Math.sqrt(1 - Math.pow(i, 2)/b/b));
					for(int j = 0; locx>centrex; j++)
					{
						locx = centrex+(int)Math.round(a*Math.sqrt(1 - Math.pow(i, 2)/b/b))-j;
						HUD_Object h = null;
						if(selected.equals(Grass.CODE))
							h = new HUD_Grass(this, locx*20, locy*20);
						else if(selected.equals(Dirt.CODE))
							h = new HUD_Dirt(this, locx*20, locy*20);
						else if(selected.equals(Water.CODE))
							h = new HUD_Water(this, locx*20, locy*20);
						else if(selected.equals(Sand.CODE))
							h = new HUD_Sand(this, locx*20, locy*20);
						else if(selected.equals(Snow.CODE))
							h = new HUD_Snow(this, locx*20, locy*20);
						else if(selected.equals(Mountain.CODE))
							h = new HUD_Mountain(this, locx*20, locy*20);
						else if(selected.equals(Tree.CODE))
							h = new HUD_Tree(this, locx*20, locy*20);
						else if(selected.equals(City.CODE))
							h = new HUD_City(this, locx*20, locy*20);
						else if(selected.equals(Fortress.CODE))
							h = new HUD_Fortress(this, locx*20, locy*20);
						else if(selected.equals(Town.CODE))
							h = new HUD_Town(this, locx*20, locy*20);
						else if(selected.equals(Camp.CODE))
							h = new HUD_Camp(this, locx*20, locy*20);
						else if(selected.equals(Landmark.CODE))
							h = new HUD_Landmark(this, locx*20, locy*20);
						addToDraw(h, locx, locy);
		//				terrain_grid[locx][locy] = new Dirt(this, locx*20, locy*20);
					}
				}
			}
		}
	}

	public void removeFromDraw(int i, int j)
	{
		draw_grid[i][j] = null;
	}

	public HUD_Object getDrawObject(int i, int j)
	{
		return draw_grid[i][j];
	}

	public void placeMultiple(int hgrid1, int vgrid1, int hgrid2, int vgrid2)
	{
		for(int i = hgrid1; i <= hgrid2; i++)
			for(int j = vgrid1; j <= vgrid2; j++)
			{
				placeObject(i, j);
			}
	}

	public void placeObject(int hgrid, int vgrid)
	{
		if(selected_terrain.equals(Grass.getCode()))
		{
			terrain_grid[hgrid][vgrid] = new Grass(this, hgrid*20, vgrid*20);
			save_file[hgrid][vgrid][0] = Grass.getCode();
		}
		else if(selected_terrain.equals(Water.getCode()))
		{
			terrain_grid[hgrid][vgrid] = new Water(this, hgrid*20, vgrid*20);
			save_file[hgrid][vgrid][0] = Water.getCode();
		}
		else if(selected_terrain.equals(Snow.getCode()))
		{
			terrain_grid[hgrid][vgrid] = new Snow(this, hgrid*20, vgrid*20);
			save_file[hgrid][vgrid][0] = Snow.getCode();
		}
		else if(selected_terrain.equals(Tree.getCode()))
		{
			terrain_grid[hgrid][vgrid] = new Tree(this, hgrid*20, vgrid*20);
			save_file[hgrid][vgrid][0] = Tree.getCode();
		}
		else if(selected_terrain.equals(Dirt.getCode()))
		{
			terrain_grid[hgrid][vgrid] = new Dirt(this, hgrid*20, vgrid*20);
			save_file[hgrid][vgrid][0] = Dirt.getCode();
		}
		else if(selected_terrain.equals(Stone.getCode()))
		{
			terrain_grid[hgrid][vgrid] = new Stone(this, hgrid*20, vgrid*20);
			save_file[hgrid][vgrid][0] = Stone.getCode();
		}
		else if(selected_terrain.equals(Sand.getCode()))
		{
			terrain_grid[hgrid][vgrid] = new Sand(this, hgrid*20, vgrid*20);
			save_file[hgrid][vgrid][0] = Sand.getCode();
		}
		else if(selected_terrain.equals(Mountain.getCode()))
		{
			terrain_grid[hgrid][vgrid] = new Mountain(this, hgrid*20, vgrid*20);
			save_file[hgrid][vgrid][0] = Mountain.getCode();
		}
		else if(selected_terrain.equals(City.getCode()))
		{
			terrain_grid[hgrid][vgrid] = createNewCity(hgrid, vgrid);
			save_file[hgrid][vgrid][0] = City.getCode();
		}
		else if(selected_terrain.equals(Fortress.getCode()))
		{
			terrain_grid[hgrid][vgrid] = createNewFortress(hgrid, vgrid);
			save_file[hgrid][vgrid][0] = Fortress.getCode();
		}
		else if(selected_terrain.equals(Town.getCode()))
		{
			terrain_grid[hgrid][vgrid] = createNewTown(hgrid, vgrid);
			save_file[hgrid][vgrid][0] = Town.getCode();
		}
		else if(selected_terrain.equals(Camp.getCode()))
		{
			terrain_grid[hgrid][vgrid] = createNewCamp(hgrid, vgrid);
			save_file[hgrid][vgrid][0] = Camp.getCode();
		}
		else if(selected_terrain.equals(Landmark.getCode()))
		{
			terrain_grid[hgrid][vgrid] = createNewLandmark(hgrid, vgrid);
			save_file[hgrid][vgrid][0] = Landmark.getCode();
		}
	}

	public boolean isNotOccupied(int i, int j)
	{
		if(object_grid[i][j] == null)
			return true;
		return false;
	}

	public void selectTerrain(int i, int j)
	{
		selected_terrain = hud_save[i-61][j];
	}

	public void clearSelectedTerrain()
	{
		selected_terrain = "";
	}

	public void editObject(int x, int y)
	{
		if(x < horizontal_size && y < vertical_size)
		{
			int gridx = x/20;
			int gridy = y/20;
			if(save_file[gridx][gridy][0].equals(City.getCode()) || save_file[gridx][gridy][0].equals(Fortress.getCode()) ||save_file[gridx][gridy][0].equals(Town.getCode()) ||save_file[gridx][gridy][0].equals(Camp.getCode()) ||save_file[gridx][gridy][0].equals(Landmark.getCode()))
			{
				boolean onlyDM = terrain_grid[gridx][gridy].isInDMMode();

				Object selectedEdit = "";

				if(terrain_grid[gridx][gridy] instanceof City || terrain_grid[gridx][gridy] instanceof Fortress || terrain_grid[gridx][gridy] instanceof Town || terrain_grid[gridx][gridy] instanceof Camp)
				{
					Object[] editOptions = { "Name", "Predominant Race", "Ruler", "Landmark",
										"Tavern 1 Name", "Tavern 1 Type", "Tavern 1 Size", "Tavern 1 Owner", "Tavern 1 Owner Race",
										"Tavern 2 Name", "Tavern 2 Type", "Tavern 2 Size", "Tavern 2 Owner", "Tavern 2 Owner Race",
										"Tavern 3 Name", "Tavern 3 Type", "Tavern 3 Size", "Tavern 3 Owner", "Tavern 3 Owner Race",
										"Tavern 4 Name", "Tavern 4 Type", "Tavern 4 Size", "Tavern 4 Owner", "Tavern 4 Owner Race",
										"NPC 1 Name", "NPC 1 Race", "NPC 1 Job",
										"NPC 2 Name", "NPC 2 Race", "NPC 2 Job",
										"NPC 3 Name", "NPC 3 Race", "NPC 3 Job",
										"NPC 4 Name", "NPC 4 Race", "NPC 4 Job",
										"NPC 5 Name", "NPC 5 Race", "NPC 5 Job",
										"Quest 1", "Quest 2", "Quest 3", "Quest 4", "Quest 5", "Quest 6", "Quest 7", "Quest 8", "Quest 9", "Quest 10",
										"Visibility"};

					selectedEdit = JOptionPane.showInputDialog(null, "What would you like to edit?", "Editing", JOptionPane.QUESTION_MESSAGE, null, editOptions, editOptions[0]);
					if(selectedEdit == null)
						selectedEdit = "";
				}
				else
				{
					Object[] editOptions = { "Name",
										"NPC 1 Name", "NPC 1 Race", "NPC 1 Job",
										"NPC 2 Name", "NPC 2 Race", "NPC 2 Job",
										"NPC 3 Name", "NPC 3 Race", "NPC 3 Job",
										"NPC 4 Name", "NPC 4 Race", "NPC 4 Job",
										"NPC 5 Name", "NPC 5 Race", "NPC 5 Job",
										"Quest 1", "Quest 2", "Quest 3", "Quest 4", "Quest 5", "Quest 6", "Quest 7", "Quest 8", "Quest 9", "Quest 10",
										"Visibility"};

					selectedEdit = JOptionPane.showInputDialog(null, "What would you like to edit?", "Editing", JOptionPane.QUESTION_MESSAGE, null, editOptions, editOptions[0]);
					if(selectedEdit == null)
						selectedEdit = "";
				}

				Landmark l = (Landmark)(terrain_grid[gridx][gridy]);
				String [] all = l.getAll();
				if(selectedEdit.equals("Name"))
				{
					String newText = JOptionPane.showInputDialog(null, "Enter new name", "Changing...", JOptionPane.QUESTION_MESSAGE);
					if(newText != null)
						all[0] = newText;
				}
				else if(selectedEdit.equals("Predominant Race"))
				{
					String newText = JOptionPane.showInputDialog(null, "Enter new race", "Changing...", JOptionPane.QUESTION_MESSAGE);
					if(newText != null)
					{
						all[1] = newText;
						all[25] = newText;
						all[28] = newText;
						all[31] = newText;
						all[34] = newText;
					}
				}
				else if(selectedEdit.equals("Ruler"))
				{
					String newText = JOptionPane.showInputDialog(null, "Enter new ruler name", "Changing...", JOptionPane.QUESTION_MESSAGE);
					if(newText != null)
						all[23] = newText;
				}
				else if(selectedEdit.equals("Landmark"))
				{
					String newText = JOptionPane.showInputDialog(null, "Enter new landmark", "Changing...", JOptionPane.QUESTION_MESSAGE);
					if(newText != null)
						all[2] = newText;
				}
				else if(selectedEdit.equals("Tavern 1 Name"))
				{
					String newText = JOptionPane.showInputDialog(null, "Enter new name", "Changing...", JOptionPane.QUESTION_MESSAGE);
					if(newText != null)
						all[3] = newText;
				}
				else if(selectedEdit.equals("Tavern 1 Type"))
				{
					String newText = JOptionPane.showInputDialog(null, "Enter new type", "Changing...", JOptionPane.QUESTION_MESSAGE);
					if(newText != null)
						all[4] = newText;
				}
				else if(selectedEdit.equals("Tavern 1 Size"))
				{
					String newText = JOptionPane.showInputDialog(null, "Enter new size", "Changing...", JOptionPane.QUESTION_MESSAGE);
					if(newText != null)
						all[5] = newText;
				}
				else if(selectedEdit.equals("Tavern 1 Owner"))
				{
					String newText = JOptionPane.showInputDialog(null, "Enter new name", "Changing...", JOptionPane.QUESTION_MESSAGE);
					if(newText != null)
						all[6] = newText;
				}
				else if(selectedEdit.equals("Tavern 1 Owner Race"))
				{
					String newText = JOptionPane.showInputDialog(null, "Enter new race", "Changing...", JOptionPane.QUESTION_MESSAGE);
					if(newText != null)
						all[7] = newText;
				}
				else if(selectedEdit.equals("Tavern 2 Name"))
				{
					String newText = JOptionPane.showInputDialog(null, "Enter new name", "Changing...", JOptionPane.QUESTION_MESSAGE);
					if(newText != null)
						all[8] = newText;
				}
				else if(selectedEdit.equals("Tavern 2 Type"))
				{
					String newText = JOptionPane.showInputDialog(null, "Enter new type", "Changing...", JOptionPane.QUESTION_MESSAGE);
					if(newText != null)
						all[9] = newText;
				}
				else if(selectedEdit.equals("Tavern 2 Size"))
				{
					String newText = JOptionPane.showInputDialog(null, "Enter new size", "Changing...", JOptionPane.QUESTION_MESSAGE);
					if(newText != null)
						all[10] = newText;
				}
				else if(selectedEdit.equals("Tavern 2 Owner"))
				{
					String newText = JOptionPane.showInputDialog(null, "Enter new name", "Changing...", JOptionPane.QUESTION_MESSAGE);
					if(newText != null)
						all[11] = newText;
				}
				else if(selectedEdit.equals("Tavern 2 Owner Race"))
				{
					String newText = JOptionPane.showInputDialog(null, "Enter new race", "Changing...", JOptionPane.QUESTION_MESSAGE);
					if(newText != null)
						all[12] = newText;
				}
				else if(selectedEdit.equals("Tavern 3 Name"))
				{
					String newText = JOptionPane.showInputDialog(null, "Enter new name", "Changing...", JOptionPane.QUESTION_MESSAGE);
					if(newText != null)
						all[13] = newText;
				}
				else if(selectedEdit.equals("Tavern 3 Type"))
				{
					String newText = JOptionPane.showInputDialog(null, "Enter new type", "Changing...", JOptionPane.QUESTION_MESSAGE);
					if(newText != null)
						all[14] = newText;
				}
				else if(selectedEdit.equals("Tavern 3 Size"))
				{
					String newText = JOptionPane.showInputDialog(null, "Enter new size", "Changing...", JOptionPane.QUESTION_MESSAGE);
					if(newText != null)
						all[15] = newText;
				}
				else if(selectedEdit.equals("Tavern 3 Owner"))
				{
					String newText = JOptionPane.showInputDialog(null, "Enter new name", "Changing...", JOptionPane.QUESTION_MESSAGE);
					if(newText != null)
						all[16] = newText;
				}
				else if(selectedEdit.equals("Tavern 3 Owner Race"))
				{
					String newText = JOptionPane.showInputDialog(null, "Enter new race", "Changing...", JOptionPane.QUESTION_MESSAGE);
					if(newText != null)
						all[17] = newText;
				}
				else if(selectedEdit.equals("Tavern 4 Name"))
				{
					String newText = JOptionPane.showInputDialog(null, "Enter new name", "Changing...", JOptionPane.QUESTION_MESSAGE);
					if(newText != null)
						all[18] = newText;
				}
				else if(selectedEdit.equals("Tavern 4 Type"))
				{
					String newText = JOptionPane.showInputDialog(null, "Enter new type", "Changing...", JOptionPane.QUESTION_MESSAGE);
					if(newText != null)
						all[19] = newText;
				}
				else if(selectedEdit.equals("Tavern 4 Size"))
				{
					String newText = JOptionPane.showInputDialog(null, "Enter new size", "Changing...", JOptionPane.QUESTION_MESSAGE);
					if(newText != null)
						all[20] = newText;
				}
				else if(selectedEdit.equals("Tavern 4 Owner"))
				{
					String newText = JOptionPane.showInputDialog(null, "Enter new name", "Changing...", JOptionPane.QUESTION_MESSAGE);
					if(newText != null)
						all[21] = newText;
				}
				else if(selectedEdit.equals("Tavern 4 Owner Race"))
				{
					String newText = JOptionPane.showInputDialog(null, "Enter new race", "Changing...", JOptionPane.QUESTION_MESSAGE);
					if(newText != null)
						all[22] = newText;
				}
				else if(selectedEdit.equals("NPC 1 Name"))
				{
					String newText = JOptionPane.showInputDialog(null, "Enter new name", "Changing...", JOptionPane.QUESTION_MESSAGE);
					if(newText != null)
						all[24] = newText;
				}
				else if(selectedEdit.equals("NPC 1 Race"))
				{
					String newText = JOptionPane.showInputDialog(null, "Enter new race", "Changing...", JOptionPane.QUESTION_MESSAGE);
					if(newText != null)
						all[25] = newText;
				}
				else if(selectedEdit.equals("NPC 1 Job"))
				{
					String newText = JOptionPane.showInputDialog(null, "Enter new job", "Changing...", JOptionPane.QUESTION_MESSAGE);
					if(newText != null)
						all[26] = newText;
				}
				else if(selectedEdit.equals("NPC 2 Name"))
				{
					String newText = JOptionPane.showInputDialog(null, "Enter new name", "Changing...", JOptionPane.QUESTION_MESSAGE);
					if(newText != null)
						all[27] = newText;
				}
				else if(selectedEdit.equals("NPC 2 Race"))
				{
					String newText = JOptionPane.showInputDialog(null, "Enter new race", "Changing...", JOptionPane.QUESTION_MESSAGE);
					if(newText != null)
						all[28] = newText;
				}
				else if(selectedEdit.equals("NPC 2 Job"))
				{
					String newText = JOptionPane.showInputDialog(null, "Enter new job", "Changing...", JOptionPane.QUESTION_MESSAGE);
					if(newText != null)
						all[29] = newText;
				}
				else if(selectedEdit.equals("NPC 3 Name"))
				{
					String newText = JOptionPane.showInputDialog(null, "Enter new name", "Changing...", JOptionPane.QUESTION_MESSAGE);
					if(newText != null)
						all[30] = newText;
				}
				else if(selectedEdit.equals("NPC 3 Race"))
				{
					String newText = JOptionPane.showInputDialog(null, "Enter new race", "Changing...", JOptionPane.QUESTION_MESSAGE);
					if(newText != null)
						all[31] = newText;
				}
				else if(selectedEdit.equals("NPC 3 Job"))
				{
					String newText = JOptionPane.showInputDialog(null, "Enter new job", "Changing...", JOptionPane.QUESTION_MESSAGE);
					if(newText != null)
						all[32] = newText;
				}
				else if(selectedEdit.equals("NPC 4 Name"))
				{
					String newText = JOptionPane.showInputDialog(null, "Enter new name", "Changing...", JOptionPane.QUESTION_MESSAGE);
					if(newText != null)
						all[33] = newText;
				}
				else if(selectedEdit.equals("NPC 4 Race"))
				{
					String newText = JOptionPane.showInputDialog(null, "Enter new race", "Changing...", JOptionPane.QUESTION_MESSAGE);
					if(newText != null)
						all[34] = newText;
				}
				else if(selectedEdit.equals("NPC 4 Job"))
				{
					String newText = JOptionPane.showInputDialog(null, "Enter new job", "Changing...", JOptionPane.QUESTION_MESSAGE);
					if(newText != null)
						all[35] = newText;
				}
				else if(selectedEdit.equals("NPC 5 Name"))
				{
					String newText = JOptionPane.showInputDialog(null, "Enter new name", "Changing...", JOptionPane.QUESTION_MESSAGE);
					if(newText != null)
						all[36] = newText;
				}
				else if(selectedEdit.equals("NPC 5 Race"))
				{
					String newText = JOptionPane.showInputDialog(null, "Enter new race", "Changing...", JOptionPane.QUESTION_MESSAGE);
					if(newText != null)
						all[37] = newText;
				}
				else if(selectedEdit.equals("NPC 5 Job"))
				{
					String newText = JOptionPane.showInputDialog(null, "Enter new job", "Changing...", JOptionPane.QUESTION_MESSAGE);
					if(newText != null)
						all[38] = newText;
				}
				else if(selectedEdit.equals("Quest 1"))
				{
					String newText = JOptionPane.showInputDialog(null, "Enter new quest", "Changing...", JOptionPane.QUESTION_MESSAGE);
					if(newText != null)
						all[39] = newText;
				}
				else if(selectedEdit.equals("Quest 2"))
				{
					String newText = JOptionPane.showInputDialog(null, "Enter new quest", "Changing...", JOptionPane.QUESTION_MESSAGE);
					if(newText != null)
						all[40] = newText;
				}
				else if(selectedEdit.equals("Quest 3"))
				{
					String newText = JOptionPane.showInputDialog(null, "Enter new quest", "Changing...", JOptionPane.QUESTION_MESSAGE);
					if(newText != null)
						all[41] = newText;
				}
				else if(selectedEdit.equals("Quest 4"))
				{
					String newText = JOptionPane.showInputDialog(null, "Enter new quest", "Changing...", JOptionPane.QUESTION_MESSAGE);
					if(newText != null)
						all[42] = newText;
				}
				else if(selectedEdit.equals("Quest 5"))
				{
					String newText = JOptionPane.showInputDialog(null, "Enter new quest", "Changing...", JOptionPane.QUESTION_MESSAGE);
					if(newText != null)
						all[43] = newText;
				}
				else if(selectedEdit.equals("Quest 6"))
				{
					String newText = JOptionPane.showInputDialog(null, "Enter new quest", "Changing...", JOptionPane.QUESTION_MESSAGE);
					if(newText != null)
						all[44] = newText;
				}
				else if(selectedEdit.equals("Quest 7"))
				{
					String newText = JOptionPane.showInputDialog(null, "Enter new quest", "Changing...", JOptionPane.QUESTION_MESSAGE);
					if(newText != null)
						all[45] = newText;
				}
				else if(selectedEdit.equals("Quest 8"))
				{
					String newText = JOptionPane.showInputDialog(null, "Enter new quest", "Changing...", JOptionPane.QUESTION_MESSAGE);
					if(newText != null)
						all[46] = newText;
				}
				else if(selectedEdit.equals("Quest 9"))
				{
					String newText = JOptionPane.showInputDialog(null, "Enter new quest", "Changing...", JOptionPane.QUESTION_MESSAGE);
					if(newText != null)
						all[47] = newText;
				}
				else if(selectedEdit.equals("Quest 10"))
				{
					String newText = JOptionPane.showInputDialog(null, "Enter new quest", "Changing...", JOptionPane.QUESTION_MESSAGE);
					if(newText != null)
						all[48] = newText;
				}
				else if(selectedEdit.equals("Visibility"))
				{
					String visibleToWho;
					do{
						visibleToWho = JOptionPane.showInputDialog(null, "Who is this visible to?\n\t[players, DM]", "Changing...", JOptionPane.QUESTION_MESSAGE);
					}while(!visibleToWho.equalsIgnoreCase("players") && !visibleToWho.equalsIgnoreCase("DM"));
					if(visibleToWho.equalsIgnoreCase("players"))
						onlyDM = false;
					else
						onlyDM = true;
				}

				if(terrain_grid[gridx][gridy] instanceof City)
				{
					terrain_grid[gridx][gridy] = new City(this, gridx*20, gridy*20, all);
				}
				else if(terrain_grid[gridx][gridy] instanceof Fortress)
				{
					terrain_grid[gridx][gridy] = new Fortress(this, gridx*20, gridy*20, all);
				}
				else if(terrain_grid[gridx][gridy] instanceof Town)
				{
					terrain_grid[gridx][gridy] = new Town(this, gridx*20, gridy*20, all);
				}
				else if(terrain_grid[gridx][gridy] instanceof Camp)
				{
					terrain_grid[gridx][gridy] = new Camp(this, gridx*20, gridy*20, all);
				}
				else if(terrain_grid[gridx][gridy] instanceof Landmark)
				{
					terrain_grid[gridx][gridy] = new Landmark(this, gridx*20, gridy*20, all);
				}

				if(onlyDM)
					terrain_grid[gridx][gridy].makeDM();
				else
					terrain_grid[gridx][gridy].makeNotDM();
			}
		}
		edit_mode = false;
	}

	public void checkMouseOver(int x, int y)
	{
		if(x < horizontal_size && y < vertical_size)
		{
			int gridx = x/20;
			int gridy = y/20;
			if(save_file[gridx][gridy][0].equals(City.getCode()) || save_file[gridx][gridy][0].equals(Fortress.getCode()) ||save_file[gridx][gridy][0].equals(Town.getCode()) ||save_file[gridx][gridy][0].equals(Camp.getCode()) ||save_file[gridx][gridy][0].equals(Landmark.getCode()))
			{
				if(terrain_grid[gridx][gridy] instanceof City)
					new LandmarkDisplay(City.CODE, gridx, gridy);
				else if(terrain_grid[gridx][gridy] instanceof Fortress)
					new LandmarkDisplay(Fortress.CODE, gridx, gridy);
				else if(terrain_grid[gridx][gridy] instanceof Town)
					new LandmarkDisplay(Town.CODE, gridx, gridy);
				else if(terrain_grid[gridx][gridy] instanceof Camp)
					new LandmarkDisplay(Camp.CODE, gridx, gridy);
				else if(terrain_grid[gridx][gridy] instanceof Landmark)
					new LandmarkDisplay(Landmark.CODE, gridx, gridy);
			}
		}
	}

	public String[][][] getSaveFile()
	{
		return save_file;
	}

	public Map_Object[][] getMapGrid()
	{
		return terrain_grid;
	}

	public void save()
	{
		String save_name = Constants.MAP_FOLDER + "\\" + JOptionPane.showInputDialog(null, "Enter save name:", "Saving...", JOptionPane.QUESTION_MESSAGE) + ".txt";
		String terrain_save_name = save_name;
		File f1 = new File(terrain_save_name);
		if(f1.delete())
			System.out.println("Overwriting previous save...");
		else
			System.out.println("Creating new save...");
		String saveTerrainString = "";
		String saveLandmarksString = "";
		PrintWriter saveTerrainWriter = openWriter(terrain_save_name);
		for(int i = 0; i < vertical_grid_size; i++)
		{
			for(int j = 0; j < horizontal_grid_size; j++)
			{
				if(j != horizontal_grid_size-1)
				{
					saveTerrainString += save_file[j][i][0] + ",";
				}
				else
				{
					saveTerrainString += save_file[j][i][0];
				}
			}
			saveTerrainWriter.println(saveTerrainString);
			saveTerrainString = "";
		}
		saveTerrainWriter.println("//locationdata//");
		for(int i = 0; i < vertical_grid_size; i++)
		{
			for(int j = 0; j < horizontal_grid_size; j++)
			{
				if(save_file[j][i][0].equals(City.getCode()) ||
					save_file[j][i][0].equals(Fortress.getCode()) ||
					save_file[j][i][0].equals(Town.getCode()) ||
					save_file[j][i][0].equals(Camp.getCode()) ||
					save_file[j][i][0].equals(Landmark.getCode()))
				{
					saveTerrainWriter.println(i);
					saveTerrainWriter.println(j);
					terrain_grid[j][i].print(saveTerrainWriter);
				}
			}
		}

		saveTerrainWriter.close();
		JOptionPane.showMessageDialog(null, "Save Complete", "", JOptionPane.INFORMATION_MESSAGE);
	}

	public void edit()
	{
		edit_mode = true;
	}

	public void load()
	{
		ArrayList<String> stored_data = new ArrayList<String>();
		String loadName = Constants.MAP_FOLDER + "\\" + JOptionPane.showInputDialog(null, "What map would you like to load?", "Loading...", JOptionPane.QUESTION_MESSAGE) + ".txt";

		boolean reading_map = true;

		BufferedReader save_loader = getReader(loadName);

		String[][] code_grid = new String[horizontal_grid_size][vertical_grid_size];

		try
		{
			line = save_loader.readLine();
		}
		catch(IOException e)
		{
			System.exit(0);
		}
		int line_count = 0;
		while(line != null)
		{
			if(line.equals("//locationdata//"))
			{
				reading_map = false;
			}
			if(reading_map)
			{
				String[] line_codes = line.split(",");
				for(int j = 0; j < line_codes.length; j++)
						code_grid[j][line_count] = line_codes[j];
				try
				{
					line = save_loader.readLine();
				}
				catch(IOException e)
				{
					System.exit(0);
				}
				line_count++;
			}
			else
			{
				try
				{
					line = save_loader.readLine();
				}
				catch(IOException e)
				{
					System.exit(0);
				}
				if(line != null)
					stored_data.add(line);
			}
		}
		for(int i = 0; i < horizontal_grid_size; i++)
			for(int j = 0; j < vertical_grid_size; j++)
			{
				if(code_grid[i][j].equals(Grass.getCode()))
				{
					terrain_grid[i][j] = new Grass(this, i*20, j*20);
					save_file[i][j][0] = Grass.getCode();
				}
				else if(code_grid[i][j].equals(Water.getCode()))
				{
					terrain_grid[i][j] = new Water(this, i*20, j*20);
					save_file[i][j][0] = Water.getCode();
				}
				else if(code_grid[i][j].equals(Snow.getCode()))
				{
					terrain_grid[i][j] = new Snow(this, i*20, j*20);
					save_file[i][j][0] = Snow.getCode();
				}
				else if(code_grid[i][j].equals(Tree.getCode()))
				{
					terrain_grid[i][j] = new Tree(this, i*20, j*20);
					save_file[i][j][0] = Tree.getCode();
				}
				else if(code_grid[i][j].equals(Stone.getCode()))
				{
					terrain_grid[i][j] = new Stone(this, i*20, j*20);
					save_file[i][j][0] = Stone.getCode();
				}
				else if(code_grid[i][j].equals(Dirt.getCode()))
				{
					terrain_grid[i][j] = new Dirt(this, i*20, j*20);
					save_file[i][j][0] = Dirt.getCode();
				}
				else if(code_grid[i][j].equals(Sand.getCode()))
				{
					terrain_grid[i][j] = new Sand(this, i*20, j*20);
					save_file[i][j][0] = Sand.getCode();
				}
				else if(code_grid[i][j].equals(Mountain.getCode()))
				{
					terrain_grid[i][j] = new Mountain(this, i*20, j*20);
					save_file[i][j][0] = Mountain.getCode();
				}
				else if(code_grid[i][j].equals(City.getCode()))
				{
					int x = 0;
					boolean leave = false;
					for(int k = 0; k < stored_data.size()-1; k++)
					{
						if(stored_data.get(k).equals(Integer.toString(j)) && stored_data.get(k+1).equals(Integer.toString(i)))
						{
							x = k;
							leave = true;
							break;
						}
						if(leave)
							break;
					}
					terrain_grid[i][j] = new City(this, i*20, j*20, stored_data.get(x+2), stored_data.get(x+3),
																	stored_data.get(x+4), stored_data.get(x+5),
																	stored_data.get(x+6), stored_data.get(x+7),
																	stored_data.get(x+8), stored_data.get(x+9),
																	stored_data.get(x+10), stored_data.get(x+11),
																	stored_data.get(x+12), stored_data.get(x+13),
																	stored_data.get(x+14), stored_data.get(x+15),
																	stored_data.get(x+16), stored_data.get(x+17),
																	stored_data.get(x+18), stored_data.get(x+19),
																	stored_data.get(x+20), stored_data.get(x+21),
																	stored_data.get(x+22), stored_data.get(x+23),
																	stored_data.get(x+24), stored_data.get(x+25),
																	stored_data.get(x+26), stored_data.get(x+27),
																	stored_data.get(x+28), stored_data.get(x+29),
																	stored_data.get(x+30), stored_data.get(x+31),
																	stored_data.get(x+32), stored_data.get(x+33),
																	stored_data.get(x+34), stored_data.get(x+35),
																	stored_data.get(x+36), stored_data.get(x+37),
																	stored_data.get(x+38), stored_data.get(x+39),
																	stored_data.get(x+40), stored_data.get(x+41),
																	stored_data.get(x+42), stored_data.get(x+43),
																	stored_data.get(x+44), stored_data.get(x+45),
																	stored_data.get(x+46), stored_data.get(x+47),
																	stored_data.get(x+48), stored_data.get(x+49),
																	stored_data.get(x+50), stored_data.get(x+51),
																	stored_data.get(x+52));
					save_file[i][j][0] = City.getCode();
					leave = false;
				}
				else if(code_grid[i][j].equals(Fortress.getCode()))
				{
					int x = 0;
					boolean leave = false;
					for(int k = 0; k < stored_data.size()-1; k++)
					{
						if(stored_data.get(k).equals(Integer.toString(j)) && stored_data.get(k+1).equals(Integer.toString(i)))
						{
							x = k;
							leave = true;
							break;
						}
						if(leave)
							break;
					}
					terrain_grid[i][j] = new Fortress(this, i*20, j*20, stored_data.get(x+2), stored_data.get(x+3),
																	stored_data.get(x+4), stored_data.get(x+5),
																	stored_data.get(x+6), stored_data.get(x+7),
																	stored_data.get(x+8), stored_data.get(x+9),
																	stored_data.get(x+10), stored_data.get(x+11),
																	stored_data.get(x+12), stored_data.get(x+13),
																	stored_data.get(x+14), stored_data.get(x+15),
																	stored_data.get(x+16), stored_data.get(x+17),
																	stored_data.get(x+18), stored_data.get(x+19),
																	stored_data.get(x+20), stored_data.get(x+21),
																	stored_data.get(x+22), stored_data.get(x+23),
																	stored_data.get(x+24), stored_data.get(x+25),
																	stored_data.get(x+26), stored_data.get(x+27),
																	stored_data.get(x+28), stored_data.get(x+29),
																	stored_data.get(x+30), stored_data.get(x+31),
																	stored_data.get(x+32), stored_data.get(x+33),
																	stored_data.get(x+34), stored_data.get(x+35),
																	stored_data.get(x+36), stored_data.get(x+37),
																	stored_data.get(x+38), stored_data.get(x+39),
																	stored_data.get(x+40), stored_data.get(x+41),
																	stored_data.get(x+42), stored_data.get(x+43),
																	stored_data.get(x+44), stored_data.get(x+45),
																	stored_data.get(x+46), stored_data.get(x+47),
																	stored_data.get(x+48), stored_data.get(x+49),
																	stored_data.get(x+50), stored_data.get(x+51),
																	stored_data.get(x+52));
					save_file[i][j][0] = Fortress.getCode();
					leave = false;
				}
				else if(code_grid[i][j].equals(Town.getCode()))
				{
					int x = 0;
					boolean leave = false;
					for(int k = 0; k < stored_data.size()-1; k++)
					{
						if(stored_data.get(k).equals(Integer.toString(j)) && stored_data.get(k+1).equals(Integer.toString(i)))
						{
							x = k;
							leave = true;
							break;
						}
						if(leave)
							break;
					}
					terrain_grid[i][j] = new Town(this, i*20, j*20, stored_data.get(x+2), stored_data.get(x+3),
																	stored_data.get(x+4), stored_data.get(x+5),
																	stored_data.get(x+6), stored_data.get(x+7),
																	stored_data.get(x+8), stored_data.get(x+9),
																	stored_data.get(x+10), stored_data.get(x+11),
																	stored_data.get(x+12), stored_data.get(x+13),
																	stored_data.get(x+14), stored_data.get(x+15),
																	stored_data.get(x+16), stored_data.get(x+17),
																	stored_data.get(x+18), stored_data.get(x+19),
																	stored_data.get(x+20), stored_data.get(x+21),
																	stored_data.get(x+22), stored_data.get(x+23),
																	stored_data.get(x+24), stored_data.get(x+25),
																	stored_data.get(x+26), stored_data.get(x+27),
																	stored_data.get(x+28), stored_data.get(x+29),
																	stored_data.get(x+30), stored_data.get(x+31),
																	stored_data.get(x+32), stored_data.get(x+33),
																	stored_data.get(x+34), stored_data.get(x+35),
																	stored_data.get(x+36), stored_data.get(x+37),
																	stored_data.get(x+38), stored_data.get(x+39),
																	stored_data.get(x+40), stored_data.get(x+41),
																	stored_data.get(x+42), stored_data.get(x+43),
																	stored_data.get(x+44), stored_data.get(x+45),
																	stored_data.get(x+46), stored_data.get(x+47),
																	stored_data.get(x+48), stored_data.get(x+49),
																	stored_data.get(x+50), stored_data.get(x+51),
																	stored_data.get(x+52));
					save_file[i][j][0] = Town.getCode();
					leave = false;
				}
				else if(code_grid[i][j].equals(Camp.getCode()))
				{
					int x = 0;
					boolean leave = false;
					for(int k = 0; k < stored_data.size()-1; k++)
					{
						if(stored_data.get(k).equals(Integer.toString(j)) && stored_data.get(k+1).equals(Integer.toString(i)))
						{
							x = k;
							leave = true;
							break;
						}
						if(leave)
							break;
					}
					terrain_grid[i][j] = new Camp(this, i*20, j*20, stored_data.get(x+2), stored_data.get(x+3),
																	stored_data.get(x+4), stored_data.get(x+5),
																	stored_data.get(x+6), stored_data.get(x+7),
																	stored_data.get(x+8), stored_data.get(x+9),
																	stored_data.get(x+10), stored_data.get(x+11),
																	stored_data.get(x+12), stored_data.get(x+13),
																	stored_data.get(x+14), stored_data.get(x+15),
																	stored_data.get(x+16), stored_data.get(x+17),
																	stored_data.get(x+18), stored_data.get(x+19),
																	stored_data.get(x+20), stored_data.get(x+21),
																	stored_data.get(x+22), stored_data.get(x+23),
																	stored_data.get(x+24), stored_data.get(x+25),
																	stored_data.get(x+26), stored_data.get(x+27),
																	stored_data.get(x+28), stored_data.get(x+29),
																	stored_data.get(x+30), stored_data.get(x+31),
																	stored_data.get(x+32), stored_data.get(x+33),
																	stored_data.get(x+34), stored_data.get(x+35),
																	stored_data.get(x+36), stored_data.get(x+37),
																	stored_data.get(x+38), stored_data.get(x+39),
																	stored_data.get(x+40), stored_data.get(x+41),
																	stored_data.get(x+42), stored_data.get(x+43),
																	stored_data.get(x+44), stored_data.get(x+45),
																	stored_data.get(x+46), stored_data.get(x+47),
																	stored_data.get(x+48), stored_data.get(x+49),
																	stored_data.get(x+50), stored_data.get(x+51),
																	stored_data.get(x+52));
					save_file[i][j][0] = Camp.getCode();
					leave = false;
				}
				else if(code_grid[i][j].equals(Landmark.getCode()))
				{
					int x = 0;
					boolean leave = false;
					for(int k = 0; k < stored_data.size()-1; k++)
					{
						if(stored_data.get(k).equals(Integer.toString(j)) && stored_data.get(k+1).equals(Integer.toString(i)))
						{
							x = k;
							leave = true;
							break;
						}
						if(leave)
							break;
					}
					terrain_grid[i][j] = new Landmark(this, i*20, j*20, stored_data.get(x+2), stored_data.get(x+51), stored_data.get(x+52));
					save_file[i][j][0] = Landmark.getCode();
					leave = false;
				}
			}
		makeAllVisible();
		DMmode = false;
	}

	public void toggleDMMode()
	{
		if(DMmode)
		{
			makeAllVisible();
			DMmode = false;
		}
		else
		{
			DMmode = !DMmode;
			for(int i = 0; i < horizontal_grid_size; i++)
				for(int j = 0; j < vertical_grid_size; j++)
				{
					terrain_grid[i][j].toggleDM();
				}
		}
	}

	public void makeAllVisible()
	{
		for(int i = 0; i < horizontal_grid_size; i++)
			for(int j = 0; j < vertical_grid_size; j++)
			{
				terrain_grid[i][j].makeVisible();
			}
	}

	public Terrain getTerrain(int i, int j) {return terrain_grid[i][j];}
	public Map_Object getObject(int i, int j) {return object_grid[i][j];}
	public String getSelectedTerrain() {return selected_terrain;}

	public int getHUDHPos() {return HUD_hpos;}
	public int getHUDVPos() {return HUD_vpos;}

	public int getHUDHDim() {return HUD_GRID_WIDTH*20;}
	public int getHUDVDim() {return HUD_GRID_HEIGHT*20;}

	public int getHorSize() {return horizontal_size;}
	public int getVerSize() {return vertical_size;}

	public boolean isInDMMode() {return DMmode;}

	public HUD_Object getHUDObject(int i, int j) {return HUD_array[i][j];}


	public Map_Object[][] getObjectGrid() {return object_grid;}

	public int getHorizSize() {return horizontal_grid_size;}
	public int getVertSize() {return vertical_grid_size;}
}

class controlButton
{
	final int horizontal_size = 40;
	final int vertical_size = 20;
	int hpos, vpos;
	Toolkit kit = Toolkit.getDefaultToolkit();
	Image image;
	public controlButton(String img, int h, int v)
	{
		image = kit.getImage(Constants.IMAGE_FOLDER + "\\" + img);
		hpos = h;
		vpos = v;
	}

	public boolean intersects(int x, int y)
	{
		return (x > getHorizontalPosition() && x < getHorizontalPosition() + getHorizontalSize() && y > getVerticalPosition() && y < getVerticalPosition() + getVerticalSize());
	}
	public void setImage(String f)
	{
		image = kit.getImage(Constants.IMAGE_FOLDER + "\\" + f);
	}

	public void setHPos(int x)
	{
		hpos = x;
	}

	public int getHorizontalSize() {return horizontal_size;}
	public int getVerticalSize() {return vertical_size;}
	public int getHorizontalPosition(){return hpos;}
	public int getVerticalPosition(){return vpos;}
	public Image getImage() {return image;}
}

class DMToggleButton extends controlButton
{
	Map referredMap;
	public DMToggleButton(Map z, int h, int v)
	{
		super("dmoff.png", h, v);
		referredMap = z;
	}
	public void setImage()
	{
		if(referredMap.isInDMMode())
		{
			setImage("dmon.png");
		}
		else
		{
			setImage("dmoff.png");
		}
	}
}

class SaveButton extends controlButton
{
	public SaveButton(int h, int v)
	{
		super("savebutton.png", h, v);
	}
}

class LoadButton extends controlButton
{
	public LoadButton(int h, int v)
	{
		super("loadbutton.png", h, v);
	}
}

class EditButton extends controlButton
{
	public EditButton(int h, int v)
	{
		super("editbutton.png", h, v);
	}
}

class PlaceMultipleButton extends controlButton
{
	public PlaceMultipleButton(int h, int v)
	{
		super("placemultiplebutton.png", h, v);
	}
}

class EncounterButton extends controlButton
{
	public EncounterButton(int h, int v)
	{
		super("encounterbutton.png", h, v);
	}
}

class MakeMonsterButton extends controlButton
{
	public MakeMonsterButton(int h, int v)
	{
		super("monsterbutton.png", h, v);
	}
}

class RemoveButton extends controlButton
{
	final int horizontal_size = 80;
	public RemoveButton(int h, int v)
	{
		super("removebutton.png", h, v);
	}
	public int getHorizontalSize() {return horizontal_size;}
}

class AddButton extends controlButton
{
	public AddButton(int h, int v)
	{
		super("addbutton.png", h, v);
	}
}

class ViewButton extends controlButton
{
	public ViewButton(int h, int v)
	{
		super("viewbutton.png", h, v);
	}
}

class TagButton extends controlButton
{
	public TagButton(int h, int v)
	{
		super("tagbutton.png", h, v);
	}
}

class RunButton extends controlButton
{
	public RunButton(int h, int v)
	{
		super("runbutton.png", h, v);
	}
}

class AttackButton extends controlButton
{
	final int horizontal_size = 80;
	int enemy_index;
	public AttackButton(int i, int h, int v)
	{
		super("attackbutton.png", h, v);
		enemy_index = i;
	}

	public int getEnemyIndex() {return enemy_index;}
	public int getHorizontalSize() {return horizontal_size;}
}

class TakeDamageButton extends controlButton
{
	final int horizontal_size = 80;
	int enemy_index;
	public TakeDamageButton(int i, int h, int v)
	{
		super("takedamagebutton.png", h, v);
		enemy_index = i;
	}

	public int getEnemyIndex() {return enemy_index;}
	public int getHorizontalSize() {return horizontal_size;}
}

class OtherAttackButton extends controlButton
{
	final int horizontal_size = 80;
	public OtherAttackButton(int h, int v)
	{
		super("otherattackbutton.png", h, v);
	}

	public int getHorizontalSize() {return horizontal_size;}
}

class AddPowerButton extends controlButton
{
	final int horizontal_size = 80;
	public AddPowerButton(int h, int v)
	{
		super("addpowerbutton.png", h, v);
	}

	public int getHorizontalSize() {return horizontal_size;}
}

class NewMonsterButton extends controlButton
{
	public NewMonsterButton(int h, int v)
	{
		super("newmonsterbutton.png", h, v);
	}
}

class HUD_Object extends Map_Object
{
	static String CODE = "hud";
	String refCode;
	public HUD_Object(String f, Map z, int h, int v)
	{
		super(f, z, h, v);
		refCode = CODE;
	}

	public static String getCode() {return CODE;}
	public String getrefCode() {System.out.println(refCode);return refCode;}
}

class HUD_Mountain extends HUD_Object
{
	static String CODE = "mntn";
	public HUD_Mountain(Map z, int h, int v)
	{
		super("mountain.png", z, h, v);
	}

	public static String getCode() {return CODE;}
}

class HUD_Grass extends HUD_Object
{
	static String CODE = "gras";
	String refCode;
	public HUD_Grass(Map z, int h, int v)
	{
		super("grass.png", z, h, v);
		refCode = CODE;
	}

	public static String getCode() {return CODE;}
	public String getrefCode() {System.out.println(refCode);return refCode;}
}

class HUD_Snow extends HUD_Object
{
	static String CODE = "snow";
	public HUD_Snow(Map z, int h, int v)
	{
		super("snow.png", z, h, v);
		refCode = CODE;
	}

	public static String getCode() {return CODE;}
}

class HUD_Tree extends HUD_Object
{
	static String CODE = "tree";
	public HUD_Tree(Map z, int h, int v)
	{
		super("tree.png", z, h, v);
	}

	public static String getCode() {return CODE;}
}

class HUD_Sand extends HUD_Object
{
	static String CODE = "sand";
	public HUD_Sand(Map z, int h, int v)
	{
		super("sand.png", z, h, v);
	}

	public static String getCode() {return CODE;}
}

class HUD_Water extends HUD_Object
{
	static String CODE = "watr";
	String refCode;
	public HUD_Water(Map z, int h, int v)
	{
		super("water.png", z, h, v);
		refCode = CODE;
	}

	public static String getCode() {return CODE;}
	public String getrefCode() {System.out.println(refCode);return refCode;}
}

class HUD_Dirt extends HUD_Object
{
	static String CODE = "dirt";
	String refCode = CODE;
	public HUD_Dirt(Map z, int h, int v)
	{
		super("dirt.png", z, h, v);
	}

	public static String getCode() {return CODE;}
	public String getrefCode() {System.out.println(refCode);return refCode;}
}

class HUD_Stone extends HUD_Object
{
	static String CODE = "ston";
	String refCode = CODE;
	public HUD_Stone(Map z, int h, int v)
	{
		super("stone.png", z, h, v);
	}

	public static String getCode() {return CODE;}
	public String getrefCode() {System.out.println(refCode);return refCode;}
}

class HUD_City extends HUD_Object
{
	static String CODE = "city";
	public HUD_City(Map z, int h, int v)
	{
		super("city.png", z, h, v);
	}

	public static String getCode() {return CODE;}
}

class HUD_Landmark extends HUD_Object
{
	static String CODE = "ldmk";
	public HUD_Landmark(Map z, int h, int v)
	{
		super("landmark.png", z, h, v);
	}

	public static String getCode() {return CODE;}
}

class HUD_Fortress extends HUD_Object
{
	static String CODE = "fort";
	public HUD_Fortress(Map z, int h, int v)
	{
		super("fortress.png", z, h, v);
	}

	public static String getCode() {return CODE;}
}

class HUD_Town extends HUD_Object
{
	static String CODE = "town";
	public HUD_Town(Map z, int h, int v)
	{
		super("town.png", z, h, v);
	}

	public static String getCode() {return CODE;}
}

class HUD_Camp extends HUD_Object
{
	static String CODE = "camp";
	public HUD_Camp(Map z, int h, int v)
	{
		super("camp.png", z, h, v);
	}

	public static String getCode() {return CODE;}
}

class Map_Object
{
	Image image;
	String imageFile;
	Map map;
	Toolkit kit = Toolkit.getDefaultToolkit();
	static String CODE = "blnk";
	boolean visible = true;
	boolean DMmode = false;
	int current_hpos, current_vpos;

	public Map_Object(String f, Map z, int h, int v)
	{
		imageFile = f;
		image = kit.getImage(Constants.IMAGE_FOLDER + "\\" + f);
		map = z;
		current_hpos = h;
		current_vpos = v;
	}

	public void act()
	{

	}

	public static String getCode() {return CODE;}

	public int getHGridLocation(int hpos)
	{
		return hpos/20;
	}

	public int getVGridLocation(int vpos)
	{
		return vpos/20;
	}

	public int getHGridLocation()
	{
		return current_hpos/20;
	}

	public int getVGridLocation()
	{
		return current_vpos/20;
	}

	public int getHPos() {return current_hpos;}
	public int getVPos() {return current_vpos;}

	public Map getMap() {return map;}
	public Image getImage()
	{
		if(visible)
			return image;
		return kit.getImage(Constants.IMAGE_FOLDER + "\\grass.png");
	}
	public void setImage(String n)
	{
		image = kit.getImage(Constants.IMAGE_FOLDER + "\\" + n);
	}

	public void toggleDM()
	{
		visible = true;
	}

	public void makeDM()
	{
		DMmode = true;
	}
	public void makeNotDM()
	{
		DMmode = false;
	}

	public void makeVisible()
	{
		visible = true;
	}

	public void makeInvisible()
	{
		visible = false;
	}

	public boolean isInDMMode()
	{
		return DMmode;
	}

	public boolean isVisible()
	{
		return visible;
	}

	public Toolkit getToolkit() {return kit;}
}

class Terrain extends Map_Object
{
	static String CODE = "terr";
	public Terrain(String f, Map z, int h, int v)
	{
		super(f, z, h, v);
	}

	public String info() {return "";}

	public void print(PrintWriter p)
	{
	}

	public static String getCode() {return CODE;}
}

class Impassable_Terrain extends Terrain
{
	static String CODE = "impt";
	public Impassable_Terrain(String f, Map z, int h, int v)
	{
		super(f, z, h, v);
	}

	public static String getCode() {return CODE;}
}

class Mountain extends Terrain
{
	static String CODE = "mntn";
	public Mountain(Map z, int h, int v)
	{
		super("mountain.png", z, h, v);
	}

	public static String getCode() {return CODE;}
}

class Snow extends Terrain
{
	static String CODE = "snow";
	public Snow(Map z, int h, int v)
	{
		super("snow.png", z, h, v);
	}

	public static String getCode() {return CODE;}
}

class Tree extends Terrain
{
	static String CODE = "tree";
	public Tree(Map z, int h, int v)
	{
		super("tree.png", z, h, v);
	}

	public static String getCode() {return CODE;}
}

class Grass extends Terrain
{
	static String CODE = "gras";
	public Grass(Map z, int h, int v)
	{
		super("grass.png", z, h, v);
	}

	public static String getCode() {return CODE;}
}

class Sand extends Terrain
{
	static String CODE = "sand";
	public Sand(Map z, int h, int v)
	{
		super("sand.png", z, h, v);
	}

	public static String getCode() {return CODE;}
}

class Water extends Terrain
{
	static String CODE = "watr";
	public Water(Map z, int h, int v)
	{
		super("water.png", z, h, v);
	}

	public static String getCode() {return CODE;}
}

class Dirt extends Terrain
{
	static String CODE = "dirt";
	public Dirt(Map z, int h, int v)
	{
		super("dirt.png", z, h, v);
	}

	public static String getCode() {return CODE;}
}

class Stone extends Terrain
{
	static String CODE = "ston";
	public Stone(Map z, int h, int v)
	{
		super("stone.png", z, h, v);
	}

	public static String getCode() {return CODE;}
}

class Landmark extends Terrain
{
	static String CODE = "ldmk";
	static String[] keywords_forest = { "forest", "wood", "glade", "hollow" };
	static String[] keywords_castle = { "castle", "fort", "tower", "tor", "keep", "ruin" };
	static String[] keywords_cave = { "cave", "tunnel", "labyrinth", "maze", "hall", "tomb" };
	static String[] keywords_mine = { "mine" };
	static String[] keywords_marsh = { "marsh", "bog", "swamp" };
	static String[] keywords_mountain = { "mount" };
	static String[] keywords_cliff = { "cliff" };
	static String[] keywords_valley = { "valley", "ravine", "gorge", "chasm", "abyss" };
	static String[] keywords_hill = { "hill", "downs" };
	static String[] keywords_farm = { "farm" };
	static String[] keywords_graveyard = { "grave" };
	static String[] keywords_cove = { "cove", "inlet" };
	static String[] keywords_chapel = { "chapel", "church" };
	static String[] keywords_temple = { "monastery", "temple" };
	static String[] keywords_shrine = { "shrine", "altar" };
	static String[] keywords_tomb = { "tomb", "barrow" };
	String name, predominant_race, notable_buildings, tavern1_name, tavern1_state, tavern1_size, tavern1_owner_name, tavern1_owner_race;
	String tavern2_name, tavern2_state, tavern2_size, tavern2_owner_name, tavern2_owner_race, tavern3_name, tavern3_state, tavern3_size, tavern3_owner_name;
	String tavern3_owner_race, tavern4_name, tavern4_state, tavern4_size, tavern4_owner_name, tavern4_owner_race, ruler_name, npc1_name, npc1_race;
	String npc2_name, npc2_race, npc3_name, npc3_race, npc4_name, npc4_race, npc5_name, npc5_race, quest1, quest2, quest3, quest4;
	String quest5, quest6, quest7, quest8, quest9, quest10;
	String npc1_job, npc2_job, npc3_job, npc4_job, npc5_job;
	boolean inhabited;
	String display_string = "";

	public Landmark(Map z, int h, int v, String n, String d, String vis)
	{
		super("landmark.png", z, h, v);
		name = n;
		predominant_race = "";
		assignImage();
		notable_buildings = "";
		tavern1_name = "";
		tavern1_state = "";
		tavern1_size = "";
		tavern1_owner_name = "";
		tavern1_owner_race = "";
		tavern2_name = "";
		tavern2_state = "";
		tavern2_size = "";
		tavern2_owner_name = "";
		tavern2_owner_race = "";
		tavern3_name = "";
		tavern3_state = "";
		tavern3_size = "";
		tavern3_owner_name = "";
		tavern3_owner_race = "";
		tavern4_name = "";
		tavern4_state = "";
		tavern4_size = "";
		tavern4_owner_name = "";
		tavern4_owner_race = "";
		ruler_name = "";
		npc1_name = "";
		npc1_race = "";
		npc1_job = "";
		npc2_name = "";
		npc2_race = "";
		npc2_job = "";
		npc3_name = "";
		npc3_race = "";
		npc3_job = "";
		npc4_name = "";
		npc4_race = "";
		npc4_job = "";
		npc5_name= "";
		npc5_race = "";
		npc5_job = "";
		quest1 = "";
		quest2 = "";
		quest3 = "";
		quest4 = "";
		quest5 = "";
		quest6 = "";
		quest7 = "";
		quest8 = "";
		quest9 = "";
		quest10 = "";
		inhabited = false;
		setInfo();
		if(d.equals("true"))
		{
			makeDM();
		}
		else
			makeNotDM();
		if(vis.equals("true"))
			makeVisible();
		else
			makeInvisible();
	}

	public Landmark(String f, Map z, int h, int v, String[] update)
	{
		super(f, z, h, v);
		name = update[0];
		predominant_race = update[1];
		assignImage();
		notable_buildings = update[2];
		tavern1_name = update[3];
		tavern1_state = update[4];
		tavern1_size = update[5];
		tavern1_owner_name = update[6];
		tavern1_owner_race = update[7];
		tavern2_name = update[8];
		tavern2_state = update[9];
		tavern2_size = update[10];
		tavern2_owner_name = update[11];
		tavern2_owner_race = update[12];
		tavern3_name = update[13];
		tavern3_state = update[14];
		tavern3_size = update[15];
		tavern3_owner_name = update[16];
		tavern3_owner_race = update[17];
		tavern4_name = update[18];
		tavern4_state = update[19];
		tavern4_size = update[20];
		tavern4_owner_name = update[21];
		tavern4_owner_race = update[22];
		ruler_name = update[23];
		npc1_name = update[24];
		npc1_race = update[25];
		npc1_job = update[26];
		npc2_name = update[27];
		npc2_race = update[28];
		npc2_job = update[29];
		npc3_name = update[30];
		npc3_race = update[31];
		npc3_job = update[32];
		npc4_name = update[33];
		npc4_race = update[34];
		npc4_job = update[35];
		npc5_name = update[36];
		npc5_race = update[37];
		npc5_job = update[38];
		quest1 = update[39];
		quest2 = update[40];
		quest3 = update[41];
		quest4 = update[42];
		quest5 = update[43];
		quest6 = update[44];
		quest7 = update[45];
		quest8 = update[46];
		quest9 = update[47];
		quest10 = update[48];
		inhabited = true;
		setInfo();
	}

	public Landmark(Map z, int h, int v, String[] update)
	{
		super("landmark.png", z, h, v);
		inhabited = false;
		name = update[0];
		predominant_race = update[1];
		assignImage();
		notable_buildings = update[2];
		tavern1_name = update[3];
		tavern1_state = update[4];
		tavern1_size = update[5];
		tavern1_owner_name = update[6];
		tavern1_owner_race = update[7];
		tavern2_name = update[8];
		tavern2_state = update[9];
		tavern2_size = update[10];
		tavern2_owner_name = update[11];
		tavern2_owner_race = update[12];
		tavern3_name = update[13];
		tavern3_state = update[14];
		tavern3_size = update[15];
		tavern3_owner_name = update[16];
		tavern3_owner_race = update[17];
		tavern4_name = update[18];
		tavern4_state = update[19];
		tavern4_size = update[20];
		tavern4_owner_name = update[21];
		tavern4_owner_race = update[22];
		ruler_name = update[23];
		npc1_name = update[24];
		npc1_race = update[25];
		npc1_job = update[26];
		npc2_name = update[27];
		npc2_race = update[28];
		npc2_job = update[29];
		npc3_name = update[30];
		npc3_race = update[31];
		npc3_job = update[32];
		npc4_name = update[33];
		npc4_race = update[34];
		npc4_job = update[35];
		npc5_name = update[36];
		npc5_race = update[37];
		npc5_job = update[38];
		quest1 = update[39];
		quest2 = update[40];
		quest3 = update[41];
		quest4 = update[42];
		quest5 = update[43];
		quest6 = update[44];
		quest7 = update[45];
		quest8 = update[46];
		quest9 = update[47];
		quest10 = update[48];
		setInfo();
	}

	public Landmark(String f, Map z, int h, int v, String disp)
	{
		super(f, z, h, v);
		display_string = disp;
	}

	public Landmark(String f, Map z, int h, int v, String n, String pd, String e, String t1n, String t1s, String t1z, String t1o, String t1r,
					String t2n, String t2s, String t2z, String t2o, String t2r, String t3n, String t3s, String t3z, String t3o, String t3r, String t4n,
					String t4s, String t4z, String t4o, String t4r, String rn, String n1, String n1r, String n1j, String n2, String n2r, String n2j, String n3,
					String n3r, String n3j, String n4, String n4r, String n4j, String n5, String n5r, String n5j, String q1, String q2, String q3, String q4,
					String q5, String q6, String q7, String q8, String q9, String q10, String d, String vis)
	{
		super(f, z, h, v);
		name = n;
		predominant_race = pd;
		notable_buildings = e;
		tavern1_name = t1n;
		tavern1_state = t1s;
		tavern1_size = t1z;
		tavern1_owner_name = t1o;
		tavern1_owner_race = t1r;
		tavern2_name = t2n;
		tavern2_state = t2s;
		tavern2_size = t2z;
		tavern2_owner_name = t2o;
		tavern2_owner_race = t2r;
		tavern3_name = t3n;
		tavern3_state = t3s;
		tavern3_size = t3z;
		tavern3_owner_name = t3o;
		tavern3_owner_race = t3r;
		tavern4_name = t4n;
		tavern4_state = t4s;
		tavern4_size = t4z;
		tavern4_owner_name = t4o;
		tavern4_owner_race = t4r;
		ruler_name = rn;
		npc1_name = n1;
		npc1_race = n1r;
		npc1_job = n1j;
		npc2_name = n2;
		npc2_race = n2r;
		npc2_job = n2j;
		npc3_name = n3;
		npc3_race = n3r;
		npc3_job = n3j;
		npc4_name = n4;
		npc4_race = n4r;
		npc4_job = n4j;
		npc5_name = n5;
		npc5_race = n5r;
		npc5_job = n5j;
		quest1 = q1;
		quest2 = q2;
		quest3 = q3;
		quest4 = q4;
		quest5 = q5;
		quest6 = q6;
		quest7 = q7;
		quest8 = q8;
		quest9 = q9;
		quest10 = q10;
		inhabited = true;
		setInfo();
		if(d.equals("true"))
			makeDM();
		else
			makeNotDM();
		if(vis.equals("true"))
			makeVisible();
		else
			makeInvisible();
	}

	public String[] getAll()
	{
		String[] all = new String[49];
		for(int i = 0; i < 49; i++)
			all[i] = "";
		all[0] = name;
		all[1] = predominant_race;
		all[2] = notable_buildings;
		all[3] = tavern1_name;
		all[4] = tavern1_state;
		all[5] = tavern1_size;
		all[6] = tavern1_owner_name;
		all[7] = tavern1_owner_race;
		all[8] = tavern2_name;
		all[9] = tavern2_state;
		all[10] = tavern2_size;
		all[11] = tavern2_owner_name;
		all[12] = tavern2_owner_race;
		all[13] = tavern3_name;
		all[14] = tavern3_state;
		all[15] = tavern3_size;
		all[16] = tavern3_owner_name;
		all[17] = tavern3_owner_race;
		all[18] = tavern4_name;
		all[19] = tavern4_state;
		all[20] = tavern4_size;
		all[21] = tavern4_owner_name;
		all[22] = tavern4_owner_race;
		all[23] = ruler_name;
		all[24] = npc1_name;
		all[25] = npc1_race;
		all[26] = npc1_job;
		all[27] = npc2_name;
		all[28] = npc2_race;
		all[29] = npc2_job;
		all[30] = npc3_name;
		all[31] = npc3_race;
		all[32] = npc3_job;
		all[33] = npc4_name;
		all[34] = npc4_race;
		all[35] = npc4_job;
		all[36] = npc5_name;
		all[37] = npc5_race;
		all[38] = npc5_job;
		all[39] = quest1;
		all[40] = quest2;
		all[41] = quest3;
		all[42] = quest4;
		all[43] = quest5;
		all[44] = quest6;
		all[45] = quest7;
		all[46] = quest8;
		all[47] = quest9;
		all[48] = quest10;
		return all;
	}

	private void assignImage()
	{
		for(int i = 0; i < keywords_forest.length; i++)
			if(name.toLowerCase().contains(keywords_forest[i]))
				setImage("tree.png");
		for(int i = 0; i < keywords_castle.length; i++)
			if(name.toLowerCase().contains(keywords_castle[i]) && predominant_race.equals(""))
				setImage("castle.png");
		for(int i = 0; i < keywords_cave.length; i++)
			if(name.toLowerCase().contains(keywords_cave[i]))
				setImage("cave.png");
		for(int i = 0; i < keywords_mine.length; i++)
			if(name.toLowerCase().contains(keywords_mine[i]))
				setImage("mine.png");
		for(int i = 0; i < keywords_marsh.length; i++)
			if(name.toLowerCase().contains(keywords_marsh[i]))
				setImage("marsh.png");
		for(int i = 0; i < keywords_mountain.length; i++)
			if(name.toLowerCase().contains(keywords_mountain[i]))
				setImage("lone_mountain.png");
		for(int i = 0; i < keywords_valley.length; i++)
			if(name.toLowerCase().contains(keywords_valley[i]))
				setImage("valley.png");
		for(int i = 0; i < keywords_hill.length; i++)
			if(name.toLowerCase().contains(keywords_hill[i]))
				setImage("hill.png");
		for(int i = 0; i < keywords_cliff.length; i++)
			if(name.toLowerCase().contains(keywords_cliff[i]))
				setImage("cliff.png");
		for(int i = 0; i < keywords_farm.length; i++)
			if(name.toLowerCase().contains(keywords_farm[i]))
				setImage("farm.png");
		for(int i = 0; i < keywords_graveyard.length; i++)
			if(name.toLowerCase().contains(keywords_graveyard[i]))
				setImage("graveyard.png");
		for(int i = 0; i < keywords_cove.length; i++)
			if(name.toLowerCase().contains(keywords_cove[i]))
				setImage("cove.png");
		for(int i = 0; i < keywords_temple.length; i++)
			if(name.toLowerCase().contains(keywords_temple[i]))
				setImage("temple.png");
		for(int i = 0; i < keywords_shrine.length; i++)
			if(name.toLowerCase().contains(keywords_shrine[i]))
				setImage("shrine.png");
		for(int i = 0; i < keywords_tomb.length; i++)
			if(name.toLowerCase().contains(keywords_tomb[i]))
				setImage("tomb.png");
	}

	public void toggleDM()
	{
		if(isInDMMode() && isVisible())
			makeInvisible();
	}

	public void print(PrintWriter p)
	{
//		51 variables
		p.println(name);
		p.println(predominant_race);
		p.println(notable_buildings);
		p.println(tavern1_name);
		p.println(tavern1_state);
		p.println(tavern1_size);
		p.println(tavern1_owner_name);
		p.println(tavern1_owner_race);
		p.println(tavern2_name);
		p.println(tavern2_state);
		p.println(tavern2_size);
		p.println(tavern2_owner_name);
		p.println(tavern2_owner_race);
		p.println(tavern3_name);
		p.println(tavern3_state);
		p.println(tavern3_size);
		p.println(tavern3_owner_name);
		p.println(tavern3_owner_race);
		p.println(tavern4_name);
		p.println(tavern4_state);
		p.println(tavern4_size);
		p.println(tavern4_owner_name);
		p.println(tavern4_owner_race);
		p.println(ruler_name);
		p.println(npc1_name);
		p.println(npc1_race);
		p.println(npc1_job);
		p.println(npc2_name);
		p.println(npc2_race);
		p.println(npc2_job);
		p.println(npc3_name);
		p.println(npc3_race);
		p.println(npc3_job);
		p.println(npc4_name);
		p.println(npc4_race);
		p.println(npc4_job);
		p.println(npc5_name);
		p.println(npc5_race);
		p.println(npc5_job);
		p.println(quest1);
		p.println(quest2);
		p.println(quest3);
		p.println(quest4);
		p.println(quest5);
		p.println(quest6);
		p.println(quest7);
		p.println(quest8);
		p.println(quest9);
		p.println(quest10);
		p.println(isInDMMode());
		p.println(isVisible());
	}

	public void setName(String nn)
	{
		name = nn;
	}

	public void setInfo()
	{
		display_string = name.toUpperCase() + "\n";
		if(inhabited)
		{
			display_string += "Predominant race: " + predominant_race + "\n";
			if(!ruler_name.equals(""))
				display_string += "Ruler: " + ruler_name + "\n";
			if(!notable_buildings.equals(""))
				display_string += "Landmarks: " + notable_buildings + "\n";
			display_string += "\n";
			if(!tavern1_name.equals(""))
			{
				display_string += "TAVERNS:\n";
				display_string += tavern1_name + "\n    " + tavern1_state + ", " + tavern1_size + "\n    " + "Owned by "
					+ tavern1_owner_name + " the " + tavern1_owner_race + "\n";
			}
			if(!tavern2_name.equals(""))
				display_string += tavern2_name + "\n    " + tavern2_state + ", " + tavern2_size + "\n    " + "Owned by "
					+ tavern2_owner_name + " the " + tavern2_owner_race + "\n";
			if(!tavern3_name.equals(""))
				display_string += tavern3_name + "\n    " + tavern3_state + ", " + tavern3_size + "\n    " + "Owned by "
					+ tavern3_owner_name + " the " + tavern3_owner_race + "\n";
			if(!tavern4_name.equals(""))
				display_string += tavern4_name + "\n    " + tavern4_state + ", " + tavern4_size + "\n    " + "Owned by "
					+ tavern4_owner_name + " the " + tavern4_owner_race + "\n";
			if(tavern1_name.equals(""))
				display_string += "NPCS:\n    ";
			else
				display_string += "\nNPCs:\n    ";
			display_string += npc1_name + " the " + npc1_race + " " + npc1_job;
			if(!npc2_name.equals(""))
				display_string += "\n    " + npc2_name + " the " + npc2_race + " " + npc2_job;
			if(!npc3_name.equals(""))
				display_string += "\n    " + npc3_name + " the " + npc3_race + " " + npc3_job;
			if(!npc4_name.equals(""))
				display_string += "\n    " + npc4_name + " the " + npc4_race + " " + npc4_job;
			if(!npc5_name.equals(""))
				display_string += "\n    " + npc5_name + " the " + npc5_race + " " + npc5_job;
			display_string += "\n\nQUESTS:\n    ";
			display_string += quest1;
			if(!quest2.equals(""))
				display_string += "\n    " + quest2;
			if(!quest3.equals(""))
				display_string += "\n    " + quest3;
			if(!quest4.equals(""))
				display_string += "\n    " + quest4;
			if(!quest5.equals(""))
				display_string += "\n    " + quest5;
			if(!quest6.equals(""))
				display_string += "\n    " + quest6;
			if(!quest7.equals(""))
				display_string += "\n    " + quest7;
			if(!quest8.equals(""))
				display_string += "\n    " + quest8;
			if(!quest9.equals(""))
				display_string += "\n    " + quest9;
			if(!quest10.equals(""))
				display_string += "\n    " + quest10;
		}
		else
			display_string = name.toUpperCase();
	}

	public String getName() {return name;}

	public String info()
	{
		return display_string;
	}

	public static String getCode() {return CODE;}

	public static String[] getFarmKeywords() {return keywords_farm;}
	public static String[] getMineKeywords() {return keywords_mine;}
	public static String[] getTempleKeywords() {return keywords_temple;}
	public static String[] getCaveKeywords() {return keywords_cave;}
	public static String[] getShrineKeywords() {return keywords_shrine;}
}

class City extends Landmark
{
	static String CODE = "city";

	static String[] forest_races = { "Elf", "Eladrin" };

	public City(Map z, int h, int v, String n, String pd, String e, String t1n, String t1s, String t1z, String t1o, String t1r,
	String t2n, String t2s, String t2z, String t2o, String t2r, String t3n, String t3s, String t3z, String t3o, String t3r, String t4n,
	String t4s, String t4z, String t4o, String t4r, String rn, String n1, String n1r, String n1j, String n2, String n2r, String n2j, String n3,
	String n3r, String n3j, String n4, String n4r, String n4j, String n5, String n5r, String n5j, String q1, String q2, String q3, String q4,
	String q5, String q6, String q7, String q8, String q9, String q10, String d, String vis)
	{
		super("city.png", z, h, v, n, pd, e, t1n, t1s, t1z, t1o, t1r, t2n, t2s, t2z, t2o, t2r, t3n, t3s, t3z, t3o, t3r, t4n, t4s,
							t4z, t4o, t4r, rn, n1, n1r, n1j, n2, n2r, n2j, n3, n3r, n3j, n4, n4r, n4j, n5, n5r, n5j, q1, q2, q3,
							q4, q5, q6, q7, q8, q9, q10, d, vis);
		for(int i = 0; i < forest_races.length; i++)
			if(pd.equals(forest_races[i]))
				setImage("treecity.png");
	}

	public City(Map z, int h, int v, String[] update)
	{
		super("city.png", z, h, v, update);
		for(int i = 0; i < forest_races.length; i++)
			if(update[1].equals(forest_races[i]))
				setImage("treecity.png");
	}

	public City(Map z, int h, int v, String disp)
	{
		super("city.png", z, h, v, disp);
	}

	public static String getCode() {return CODE;}
}

class Fortress extends Landmark
{
	static String CODE = "fort";

	public Fortress(Map z, int h, int v, String n, String pd, String e, String t1n, String t1s, String t1z, String t1o, String t1r,
	String t2n, String t2s, String t2z, String t2o, String t2r, String t3n, String t3s, String t3z, String t3o, String t3r, String t4n,
	String t4s, String t4z, String t4o, String t4r, String rn, String n1, String n1r, String n1j, String n2, String n2r, String n2j, String n3,
	String n3r, String n3j, String n4, String n4r, String n4j, String n5, String n5r, String n5j, String q1, String q2, String q3, String q4,
	String q5, String q6, String q7, String q8, String q9, String q10, String d, String vis)
	{
		super("fortress.png", z, h, v, n, pd, e, t1n, t1s, t1z, t1o, t1r, t2n, t2s, t2z, t2o, t2r, t3n, t3s, t3z, t3o, t3r, t4n, t4s,
							t4z, t4o, t4r, rn, n1, n1r, n1j, n2, n2r, n2j, n3, n3r, n3j, n4, n4r, n4j, n5, n5r, n5j, q1, q2, q3,
							q4, q5, q6, q7, q8, q9, q10, d, vis);
	}

	public Fortress(Map z, int h, int v, String[] update)
	{
		super("fortress.png", z, h, v, update);
	}

	public Fortress(Map z, int h, int v, String disp)
	{
		super("fortress.png", z, h, v, disp);
	}

	public static String getCode() {return CODE;}
}

class Town extends Landmark
{
	static String CODE = "town";

	static String[] forest_races = { "Elf", "Eladrin" };

	public Town(Map z, int h, int v, String n, String pd, String e, String t1n, String t1s, String t1z, String t1o, String t1r,
	String t2n, String t2s, String t2z, String t2o, String t2r, String t3n, String t3s, String t3z, String t3o, String t3r, String t4n,
	String t4s, String t4z, String t4o, String t4r, String rn, String n1, String n1r, String n1j, String n2, String n2r, String n2j, String n3,
	String n3r, String n3j, String n4, String n4r, String n4j, String n5, String n5r, String n5j, String q1, String q2, String q3, String q4,
	String q5, String q6, String q7, String q8, String q9, String q10, String d, String vis)
	{
		super("town.png", z, h, v, n, pd, e, t1n, t1s, t1z, t1o, t1r, t2n, t2s, t2z, t2o, t2r, t3n, t3s, t3z, t3o, t3r, t4n, t4s,
							t4z, t4o, t4r, rn, n1, n1r, n1j, n2, n2r, n2j, n3, n3r, n3j, n4, n4r, n4j, n5, n5r, n5j, q1, q2, q3,
							q4, q5, q6, q7, q8, q9, q10, d, vis);
		for(int i = 0; i < forest_races.length; i++)
			if(pd.equals(forest_races[i]))
				setImage("treecity.png");
	}

	public Town(Map z, int h, int v, String[] update)
	{
		super("town.png", z, h, v, update);
		for(int i = 0; i < forest_races.length; i++)
			if(update[1].equals(forest_races[i]))
				setImage("treecity.png");
	}

	public Town(Map z, int h, int v, String disp)
	{
		super("town.png", z, h, v, disp);
	}

	public static String getCode() {return CODE;}
}

class Camp extends Landmark
{
	static String CODE = "camp";

	public Camp(Map z, int h, int v, String n, String pd, String e, String t1n, String t1s, String t1z, String t1o, String t1r,
	String t2n, String t2s, String t2z, String t2o, String t2r, String t3n, String t3s, String t3z, String t3o, String t3r, String t4n,
	String t4s, String t4z, String t4o, String t4r, String rn, String n1, String n1r, String n1j, String n2, String n2r, String n2j, String n3,
	String n3r, String n3j, String n4, String n4r, String n4j, String n5, String n5r, String n5j, String q1, String q2, String q3, String q4,
	String q5, String q6, String q7, String q8, String q9, String q10, String d, String vis)
	{
		super("camp.png", z, h, v, n, pd, e, t1n, t1s, t1z, t1o, t1r, t2n, t2s, t2z, t2o, t2r, t3n, t3s, t3z, t3o, t3r, t4n, t4s,
							t4z, t4o, t4r, rn, n1, n1r, n1j, n2, n2r, n2j, n3, n3r, n3j, n4, n4r, n4j, n5, n5r, n5j, q1, q2, q3,
							q4, q5, q6, q7, q8, q9, q10, d, vis);
		for(int i = 0; i < Landmark.getFarmKeywords().length; i++)
			if(n.toLowerCase().contains(Landmark.getFarmKeywords()[i]))
				setImage("farm.png");
		for(int i = 0; i < Landmark.getMineKeywords().length; i++)
			if(n.toLowerCase().contains(Landmark.getMineKeywords()[i]))
				setImage("mine.png");
		for(int i = 0; i < Landmark.getTempleKeywords().length; i++)
			if(n.toLowerCase().contains(Landmark.getTempleKeywords()[i]))
				setImage("temple.png");
		for(int i = 0; i < Landmark.getCaveKeywords().length; i++)
			if(n.toLowerCase().contains(Landmark.getCaveKeywords()[i]))
				setImage("cave.png");
		for(int i = 0; i < Landmark.getShrineKeywords().length; i++)
			if(n.toLowerCase().contains(Landmark.getShrineKeywords()[i]))
				setImage("shrine.png");
	}

	public Camp(Map z, int h, int v, String[] update)
	{
		super("camp.png", z, h, v, update);
		name = update[0];
		for(int i = 0; i < Landmark.getFarmKeywords().length; i++)
			if(name.toLowerCase().contains(Landmark.getFarmKeywords()[i]))
				setImage("farm.png");
		for(int i = 0; i < Landmark.getMineKeywords().length; i++)
			if(name.toLowerCase().contains(Landmark.getMineKeywords()[i]))
				setImage("mine.png");
		for(int i = 0; i < Landmark.getTempleKeywords().length; i++)
			if(name.toLowerCase().contains(Landmark.getTempleKeywords()[i]))
				setImage("temple.png");
		for(int i = 0; i < Landmark.getCaveKeywords().length; i++)
			if(name.toLowerCase().contains(Landmark.getCaveKeywords()[i]))
				setImage("cave.png");
		for(int i = 0; i < Landmark.getShrineKeywords().length; i++)
			if(name.toLowerCase().contains(Landmark.getShrineKeywords()[i]))
				setImage("shrine.png");
	}

	public Camp(Map z, int h, int v, String disp)
	{
		super("camp.png", z, h, v, disp);
	}

	public static String getCode() {return CODE;}
}

class Combatant
{
	String name;
	int initiative, initiative_mod, hp, attack, number_dice, damage_die, damage_mod;
	boolean dead = false;

	public Combatant(String n, int i, int im, int h, int a, int nd, int dd, int dm)
	{
		name = n;
		initiative = i;
		initiative_mod = im;
		hp = h;
		attack = a;
		number_dice = nd;
		damage_die = dd;
		damage_mod = dm;
	}

	public String getName()
	{
		return name;
	}

	public int getInitiative()
	{
		return initiative;
	}

	public void setInitiative()
	{
		initiative = (int)(Math.random()*20+1+initiative_mod);
	}

	public int getAttack()
	{
		int roll = (int)(Math.random()*20+1);
		if(roll == 20)
			return -1;
		return roll+attack;
	}

	public int getDamage()
	{
		int tmpSum = 0;
		for(int i = 0; i < number_dice; i++)
			tmpSum += (int)(Math.random()*damage_die+1);
		return tmpSum+damage_mod;
	}

	public int getMaxDamage()
	{
		return damage_die*number_dice+damage_mod;
	}

	public void doDamage(int d)
	{
		hp -= d;
		if(hp <= 0)
		{
			hp = 0;
			dead = true;
		}
	}

	public boolean isDead()
	{
		return dead;
	}

	public int getHealth()
	{
		return hp;
	}
}

class Enemy extends Combatant
{
	public Enemy(String n, int im, int h, int a, int nd, int dd, int dm)
	{
		super(n, 0, im, h, a, nd, dd, dm);
		setInitiative();
	}
}

class Player extends Combatant
{
	public Player(String n, int i)
	{
		super(n, i, 0, 0, 0, 0, 0, 0);
	}
}

class Other_Power
{
	String name, description, action_type, power_type, recharge;
	boolean recharge_value = true;

	public Other_Power(String n, String at, String pt, String r, String d)
	{
		name = n;
		action_type = at;
		power_type = pt;
		recharge = r;
		description = d;
		if(recharge == null)
			recharge_value = false;
	}

	public String getName(){return name;}
	public String getActionType(){return action_type;}
	public String getPowerType(){return power_type;}
	public String getRecharge()
	{
		if(recharge_value)
			return recharge;
		return "";
	}
	public String getDescription(){return description;}
}

class Power
{
	int attack, number_of_dice, range, area, damage_mod;

	static final String CLOSE_BURST = "Close Burst";
	static final String CLOSE_BLAST = "Close Blast";
	static final String BURST = "Burst";
	static final String BLAST = "Blast";
	static final String MELEE_TOUCH = "Melee Touch";
	static final String MELEE = "Melee";
	static final String RANGED = "Ranged";
	static final String WALL = "Wall";

	static final String STANDARD = "Standard";
	static final String MOVE = "Move";
	static final String MINOR = "Minor";
	static final String IMMEDIATE = "Immediate";

	static final String AT_WILL = "At-will";
	static final String ENCOUNTER = "Encounter";
	static final String DAILY = "Daily";
	static final String RECHARGE = "Recharge";

	static final String NORMAL = "Normal";
	static final String FIRE = "Fire";
	static final String COLD = "Cold";
	static final String RADIANT = "Radiant";
	static final String NECROTIC = "Necrotic";
	static final String ACID = "Acid";
	static final String PSYCHIC = "Psychic";
	static final String LIGHTNING = "Lightning";
	static final String POISON = "Poison";
	static final String FORCE = "Force";
	static final String THUNDER = "Thunder";

	static final String AC = "AC";
	static final String FORTITUDE = "Fortitude";
	static final String REFLEX = "Reflex";
	static final String WILL = "Will";

	String name, attack_type, damage_type, defence, die, action_type, power_type, recharge, description;
	boolean recharge_value = true;

	public Power(String n, String at, String dt, int atk, int a, int ran, String vs, int nd, String d, int dm, String act, String pt, String r, String des)
	{
		name = n;
		attack_type = at;
		damage_type = dt;
		attack = atk;
		area = a;
		range = ran;
		defence = vs;
		number_of_dice = nd;
		die = d;
		damage_mod = dm;
		action_type = act;
		power_type = pt;
		recharge = r;
		description = des;
		if(recharge == null)
			recharge_value = false;
	}

	public String getName(){return name;}
	public int getArea(){return area;}
	public int getRange(){return range;}
	public String getDamageType(){return damage_type;}
	public String getAttackType(){return attack_type;}
	public int getAttack(){return attack;}
	public String getDefence(){return defence;}
	public int getNumberOfDice(){return number_of_dice;}
	public String getDie(){return die;}
	public int getDamageMod(){return damage_mod;}
	public String getActionType(){return action_type;}
	public String getPowerType(){return power_type;}
	public String getRecharge()
	{
		if(recharge_value)
			return recharge;
		return "";
	}
	public String getDescription(){return description;}
}

class LandmarkDisplay extends JFrame
{
	static final int DISPLAY_FRAME_H = 1000;
	static final int DISPLAY_FRAME_V = 600;

	String[] data;
	Toolkit kit = Toolkit.getDefaultToolkit();
	Image background;

	int gridx, gridy;

	EditButton eb = new EditButton(DISPLAY_FRAME_H/2-20, DISPLAY_FRAME_V-60);

	public LandmarkDisplay(String type, int x, int y)
	{
    	if(type.equals(City.CODE))
    		background = kit.getImage(Constants.IMAGE_FOLDER + "\\citybackground.png");
    	else if(type.equals(Fortress.CODE))
    		background = kit.getImage(Constants.IMAGE_FOLDER + "\\fortressbackground.png");
    	else if(type.equals(Town.CODE))
    		background = kit.getImage(Constants.IMAGE_FOLDER + "\\townbackground.png");
    	else if(type.equals(Camp.CODE))
    		background = kit.getImage(Constants.IMAGE_FOLDER + "\\campbackground.png");
    	else if(type.equals(Landmark.CODE))
    		background = kit.getImage(Constants.IMAGE_FOLDER + "\\landmarkbackground.png");

    	gridx = x;
    	gridy = y;
    	set();
	}

	private void set()
	{
		this.setSize(DISPLAY_FRAME_H, DISPLAY_FRAME_V);
    	this.add(new Screen(), BorderLayout.CENTER);
    	this.setVisible(true);
    	this.setLocationRelativeTo(null);
    	this.setResizable(false);
    	getData();
	}

	private void getData()
	{
		Landmark l = (Landmark)MapMaker.MAP.getMapGrid()[gridx][gridy];
		data = l.getAll();
    	this.setTitle(data[0]);
	}

	private class Screen extends JComponent implements ActionListener
	{
		Timer t = new Timer(50, this);
		Graphics2D g2;
		Graphics graphics;
		int spacingY, currentX, currentY;

		public Screen()
		{
			t.start();
			this.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e)
			{
				if(e.getButton() == MouseEvent.BUTTON1)
				{
					if(eb.intersects(e.getX(), e.getY()))
					{
						MapMaker.MAP.editObject(gridx*20, gridy*20);
					}
				}
			}
			});
		}

		public void paint(Graphics g)
		{
			/*
				all[0] = name;
				all[1] = predominant_race;
				all[2] = notable_buildings;
				all[3] = tavern1_name;
				all[4] = tavern1_state;
				all[5] = tavern1_size;
				all[6] = tavern1_owner_name;
				all[7] = tavern1_owner_race;
				all[8] = tavern2_name;
				all[9] = tavern2_state;
				all[10] = tavern2_size;
				all[11] = tavern2_owner_name;
				all[12] = tavern2_owner_race;
				all[13] = tavern3_name;
				all[14] = tavern3_state;
				all[15] = tavern3_size;
				all[16] = tavern3_owner_name;
				all[17] = tavern3_owner_race;
				all[18] = tavern4_name;
				all[19] = tavern4_state;
				all[20] = tavern4_size;
				all[21] = tavern4_owner_name;
				all[22] = tavern4_owner_race;
				all[23] = ruler_name;
				all[24] = npc1_name;
				all[25] = npc1_race;
				all[26] = npc1_job;
				all[27] = npc2_name;
				all[28] = npc2_race;
				all[29] = npc2_job;
				all[30] = npc3_name;
				all[31] = npc3_race;
				all[32] = npc3_job;
				all[33] = npc4_name;
				all[34] = npc4_race;
				all[35] = npc4_job;
				all[36] = npc5_name;
				all[37] = npc5_race;
				all[38] = npc5_job;
				all[39] = quest1;
				all[40] = quest2;
				all[41] = quest3;
				all[42] = quest4;
				all[43] = quest5;
				all[44] = quest6;
				all[45] = quest7;
				all[46] = quest8;
				all[47] = quest9;
				all[48] = quest10;
			*/
			getData();
			g2 = (Graphics2D)g;
			graphics = g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.drawImage(background, 0, 0, this);

			g.drawImage(eb.getImage(), eb.getHorizontalPosition(), eb.getVerticalPosition(), this);

			int startX = 20;
			int startY = 40;
			currentX = startX;
			currentY = startY;
			spacingY = 20;

			if(!data[1].equals("") && !data[23].equals(""))
			{
				g.setColor(Color.BLACK);
				g.fillRect(currentX-15, currentY-25, 210, 65);
				g.setColor(Color.WHITE);
				g.fillRect(currentX-10, currentY-20, 200, 55);
				g.setColor(Color.BLACK);
			}
			else if(!data[1].equals(""))
			{
				g.setColor(Color.BLACK);
				g.fillRect(currentX-15, currentY-25, 210, 45);
				g.setColor(Color.WHITE);
				g.fillRect(currentX-10, currentY-20, 200, 35);
				g.setColor(Color.BLACK);
			}

			if(!data[1].equals(""))
			{
				write("Predominant Race: " + data[1]);
			}
			if(!data[23].equals(""))
			{
				write("Ruler: " + data[23]);
			}

			if(data[2].equals(""))
			{
				currentY = startY;
				currentX = DISPLAY_FRAME_H/2-data[0].length()*3;

				int len;
				if(data[0].length() < 10)
					len = data[0].length()*8+10;
				else
					len = data[0].length()*7+10;

				g.setColor(Color.BLACK);
				g.fillRect(DISPLAY_FRAME_H/2-data[0].length()*3-15, currentY-25, len+10, 40);

				g.setColor(Color.WHITE);
				g.fillRect(DISPLAY_FRAME_H/2-data[0].length()*3-10, currentY-20, len, 30);
				g.setColor(Color.BLACK);

				write(data[0]);
			}
			else
			{
				currentY = startY;
				String tmplandmarks = "Landmarks: " + data[2];
				int len;
				if(tmplandmarks.length() < 30)
					len = tmplandmarks.length()*7+10;
				else
					len = tmplandmarks.length()*6+10;

				g.setColor(Color.BLACK);
				g.fillRect(DISPLAY_FRAME_H/2-tmplandmarks.length()*3-15, currentY-25, len+10, 60);

				g.setColor(Color.WHITE);
				g.fillRect(DISPLAY_FRAME_H/2-tmplandmarks.length()*3-10, currentY-20, len, 50);
				g.setColor(Color.BLACK);
				currentX = DISPLAY_FRAME_H/2-data[0].length()*3;
				write(data[0]);
				currentX = DISPLAY_FRAME_H/2-tmplandmarks.length()*3;
				write("Landmarks: " + data[2]);
			}

			currentX = DISPLAY_FRAME_H-250;
			currentY = startY;

			int numTav = 0;
			for(int i = 0; i < 4; i++)
			{
				if(!data[3+5*i].equals(""))
					numTav++;
			}

			if(!data[3].equals(""))
			{
				g.setColor(Color.BLACK);
				g.fillRect(currentX-15, currentY-25, 260, 300/4*numTav+10);
				g.setColor(Color.WHITE);
				g.fillRect(currentX-10, currentY-20, 250, 300/4*numTav);
				g.setColor(Color.BLACK);
			}

			for(int i = 0; i < 4; i++)
			{
				if(!data[3+5*i].equals(""))
				{
					writeTavern(data[3+5*i], data[4+5*i], data[5+5*i], data[6+5*i], data[7+5*i]);
					currentY += spacingY+10;
				}
			}

			currentX = DISPLAY_FRAME_H-250;
			currentY = DISPLAY_FRAME_V-130;

			if(!data[24].equals(""))
			{
				g.setColor(Color.BLACK);
				g.fillRect(currentX-15, currentY-25, 260, 120);
				g.setColor(Color.WHITE);
				g.fillRect(currentX-10, currentY-20, 250, 110);
				g.setColor(Color.BLACK);
			}

			for(int i = 0; i < Map.NPCS; i++)
			{
				if(!data[24+3*i].equals(""))
				{
					writeNPC(data[24+3*i], data[25+3*i], data[26+3*i]);
				}
			}

			currentX = startX;
			currentY = DISPLAY_FRAME_V-230;

			if(!data[39].equals(""))
			{
				g.setColor(Color.BLACK);
				g.fillRect(currentX-15, currentY-25, 260, 220);
				g.setColor(Color.WHITE);
				g.fillRect(currentX-10, currentY-20, 250, 210);
				g.setColor(Color.BLACK);
			}

			for(int i = 0; i < 10; i++)
			{
				if(!data[39+i].equals(""))
					write(data[39+i]);
			}
		}

		public void actionPerformed(ActionEvent e)
		{
			repaint();
		}

		private void write(String s)
		{
			graphics.drawString(s, currentX, currentY);
			currentY += spacingY;
		}

		private void writeNPC(String s1, String s2, String s3)
		{
			write(s1 + " the " + s2 + " " + s3);
		}

		private void writeTavern(String s1, String s2, String s3, String s4, String s5)
		{
			int old_spacing = spacingY;
//			spacingY = 10;
			write(s1);
			int old_x = currentX;
			currentX += 20;
			write(s3+ ", " + s2);
			spacingY = 0;
			write("Owned by " + s4 + " the " + s5);
			spacingY = old_spacing;
			currentX = old_x;
		}
	}
}

class MonsterMaker extends JFrame
{
	static final int MONSTER_FRAME_HORIZONTAL_SIZE = 500;
	static final int MONSTER_FRAME_VERTICAL_SIZE = 500;

	ArrayList<String> monsters = new ArrayList<String>();

	Toolkit kit = Toolkit.getDefaultToolkit();
	Image background = kit.getImage(Constants.IMAGE_FOLDER + "\\monsterbackground.png");
	AddPowerButton apb1 = new AddPowerButton(400, 430);
	EditButton emb1 = new EditButton(440, 400);
	SaveButton save1 = new SaveButton(440, 370);
	LoadButton load1 = new LoadButton(440, 340);
	NewMonsterButton new1 = new NewMonsterButton(440, 310);

	boolean epic_mode = false;
	String name, vision;
	String type = "Normal";
	Object role;
	Object main_attribute, main1, main2, main3;
	int astrength, aconstitution, aintelligence, adexterity, acharisma, awisdom, ascore;
	int level, health, ac, fort, ref, will, strength, constitution,
		intelligence, dexterity, charisma, wisdom, initiative,
		attack_vs_ac, attack_vs_other, attack_vs_multiple, speed;
	int strmod, conmod, dexmod, intmod, wismod, chamod;
	int acrobatics, arcana, athletics, bluff, diplomacy, dungeoneering, endurance, heal, history, insight, intimidate, nature,
		perception, religion, stealth, streetwise, thievery;
	int xp;

	boolean done_creating = false;
	boolean level_changed = true;

	boolean name_changed = false;
	String old_name = "";

	ArrayList<String> trainable_skills = new ArrayList<String>();
	ArrayList<Power> powers = new ArrayList<Power>();
	ArrayList<Other_Power> other_powers = new ArrayList<Other_Power>();
	ArrayList<String> trained_skills = new ArrayList<String>();

	ArrayList<String> normal_data = new ArrayList<String>();
	ArrayList<String> power_data = new ArrayList<String>();
	ArrayList<String> other_power_data = new ArrayList<String>();

	public MonsterMaker()
	{
		this.setSize(MONSTER_FRAME_HORIZONTAL_SIZE, MONSTER_FRAME_VERTICAL_SIZE);
		this.setTitle("MonsterMaker");
		this.add(new Screen(), BorderLayout.CENTER);
    	this.setVisible(true);
    	this.setLocationRelativeTo(null);
    	this.setResizable(false);
    	getMonsters();
	}

	public MonsterMaker(String load_monster)
	{
		this.setSize(MONSTER_FRAME_HORIZONTAL_SIZE, MONSTER_FRAME_VERTICAL_SIZE);
		this.setTitle("MonsterMaker");
		this.add(new Screen(), BorderLayout.CENTER);
    	this.setVisible(true);
    	this.setLocationRelativeTo(null);
    	this.setResizable(false);
    	getMonsters();
    	load(load_monster);
	}

	private int experience()
	{
		int xp;
		if(level < 6)
			xp = 100+(level-1)*25;
		else if(level < 10)
			xp = 250+(level-6)*50;
		else if(level == 10)
			xp = 500;
		else if(level < 14)
			xp = 600+(level-11)*100;
		else if(level < 18)
			xp = 1000+(level-14)*200;
		else if(level < 22)
			xp = 2000+(level-18)*400;
		else if(level < 26)
			xp = 4150+(level-22)*950;
		else if(level < 30)
			xp = 9000+(level-26)*2000;
		else
			xp = 19000;
		if(epic_mode)
			xp /= 2;
		return xp;
	}

	private void getMonsters()
	{
		String loadName = Constants.DATA_FOLDER + "\\" + Constants.MONSTER_FOLDER + "\\" + "monsters.txt";

		BufferedReader monster_loader = getReader(loadName);

		String line = "";

		try
		{
			line = monster_loader.readLine();
		}
		catch(IOException e)
		{
			System.exit(0);
		}

		while(line != null)
		{
			monsters.add(line);
			try
			{
				line = monster_loader.readLine();
			}
			catch(IOException e)
			{
				System.exit(0);
			}
		}

		sortMonsters();
	}

	private void sortMonsters()
	{
		String[] tmpmonsters = new String[monsters.size()];
		for(int i = 0; i < monsters.size(); i++)
			tmpmonsters[i] = monsters.get(i);
		Arrays.sort(tmpmonsters);

		monsters.clear();
		for(String s : tmpmonsters)
			monsters.add(s);
	}

	private void createMonster()
	{
		trainable_skills.add("Acrobatics");
		trainable_skills.add("Arcana");
		trainable_skills.add("Athletics");
		trainable_skills.add("Bluff");
		trainable_skills.add("Diplomacy");
		trainable_skills.add("Dungeoneering");
		trainable_skills.add("Endurance");
		trainable_skills.add("Heal");
		trainable_skills.add("History");
		trainable_skills.add("Insight");
		trainable_skills.add("Intimidate");
		trainable_skills.add("Nature");
		trainable_skills.add("Perception");
		trainable_skills.add("Religion");
		trainable_skills.add("Stealth");
		trainable_skills.add("Streetwise");
		trainable_skills.add("Thievery");

		trained_skills.clear();
		powers.clear();
		other_powers.clear();

		done_creating = false;

		name = JOptionPane.showInputDialog(null, "Name the monster", "Creating monster...", JOptionPane.QUESTION_MESSAGE);
		if(name == null)
			name = "Unnamed";

		String in = "";
		while(!isInteger(in))
			in = JOptionPane.showInputDialog(null, "Enter monster level", name, JOptionPane.QUESTION_MESSAGE);
		level = Integer.parseInt(in);

		Object[] epicMode = {"Yes", "No"};
		Object epic = JOptionPane.showInputDialog(null, "Epic Mode?", name, JOptionPane.QUESTION_MESSAGE, null, epicMode, epicMode[0]);
		if(epic == null || epic == "Yes")
			epic_mode = true;
		else
			epic_mode = false;

		xp = experience();

		Object[] roleOptions = {
								"Skirmisher",
								"Brute",
								"Soldier",
								"Lurker",
								"Controller",
								"Artillery"
								};
		role = JOptionPane.showInputDialog(null, "Select a role", name, JOptionPane.QUESTION_MESSAGE, null, roleOptions, roleOptions[0]);
		if(role == null)
			role = roleOptions[0];

		Object[] attOptions = {"Strength", "Constitution"};
		main1 = JOptionPane.showInputDialog(null, "Select the more relevant attribute", name, JOptionPane.QUESTION_MESSAGE, null, attOptions, attOptions[0]);
		if(main1 == null)
		{
			main1 = attOptions[0];
		}
		Object[] attOptions2 = {"Dexterity", "Intelligence"};
		main2 = JOptionPane.showInputDialog(null, "Select the more relevant attribute", name, JOptionPane.QUESTION_MESSAGE, null, attOptions2, attOptions2[0]);
		if(main2 == null)
		{
			main2 = attOptions2[0];
		}
		Object[] attOptions3 = {"Wisdom", "Charisma"};
		main3 = JOptionPane.showInputDialog(null, "Select the more relevant attribute", name, JOptionPane.QUESTION_MESSAGE, null, attOptions3, attOptions3[0]);
		if(main3 == null)
		{
			main3 = attOptions3[0];
		}

		ascore = 13+level/2;

		if(main1.equals("Strength"))
		{
			astrength = 13+level/2;
			aconstitution = 10;
		}
		else
		{
			aconstitution = 13+level/2;
			astrength = 10;
		}
		if(main2.equals("Dexterity"))
		{
			adexterity = 13+level/2;
			aintelligence = 10;
		}
		else
		{
			aintelligence = 13+level/2;
			adexterity = 10;
		}
		if(main3.equals("Wisdom"))
		{
			awisdom = 13+level/2;
			acharisma = 10;
		}
		else
		{
			acharisma = 13+level/2;
			awisdom = 10;
		}

		Object[] mainAttOptions = {
						"Strength",
						"Constitution",
						"Dexterity",
						"Intelligence",
						"Wisdom",
						"Charisma"
						};
		main_attribute = JOptionPane.showInputDialog(null, "Select the main attribute", name, JOptionPane.QUESTION_MESSAGE, null, mainAttOptions, mainAttOptions[0]);
		if(main_attribute == null)
		{
			main_attribute = mainAttOptions[0];
		}
		strength = astrength;
		constitution = aconstitution;
		dexterity = adexterity;
		intelligence = aintelligence;
		wisdom = awisdom;
		charisma = acharisma;

		if(main_attribute.equals("Strength"))
			strength += 3;
		else if(main_attribute.equals("Constitution"))
			constitution += 3;
		else if(main_attribute.equals("Dexterity"))
			dexterity += 3;
		else if(main_attribute.equals("Intelligence"))
			intelligence += 3;
		else if(main_attribute.equals("Wisdom"))
			wisdom += 3;
		else
			charisma += 3;

		strmod = strength/2-5+level/2;
		conmod = constitution/2-5+level/2;
		dexmod = dexterity/2-5+level/2;
		intmod = intelligence/2-5+level/2;
		wismod = wisdom/2-5+level/2;
		chamod = charisma/2-5+level/2;

		Object[] visionOptions = {
						"Darkvision",
						"Low-light Vision",
						"Normal"
						};
		Object vision_select;
		vision_select = JOptionPane.showInputDialog(null, "Select the level of vision", name, JOptionPane.QUESTION_MESSAGE, null, visionOptions, visionOptions[0]);
		if(vision_select == null)
		{
			vision_select = visionOptions[0];
		}

		vision = (String)vision_select;

		in = "";
		while(!isInteger(in))
			in = JOptionPane.showInputDialog(null, "Enter monster speed", name, JOptionPane.QUESTION_MESSAGE);
		speed = Integer.parseInt(in);

		Object[] skillOptions = {
						"Done",
						"Acrobatics",
						"Arcana",
						"Athletics",
						"Bluff",
						"Diplomacy",
						"Dungeoneering",
						"Endurance",
						"Heal",
						"History",
						"Insight",
						"Intimidate",
						"Nature",
						"Perception",
						"Religion",
						"Stealth",
						"Streetwise",
						"Thievery",
						};

		String skill = (String)JOptionPane.showInputDialog(null, "Select a trained skill, or choose 'Done'", name, JOptionPane.QUESTION_MESSAGE, null, skillOptions, skillOptions[0]);
		if(skill == null)
			skill = "Done";
		while(!skill.equals("Done"))
		{
			trained_skills.add(skill);
			skill = (String)JOptionPane.showInputDialog(null, "Select a trained skill, or choose 'Done'", name, JOptionPane.QUESTION_MESSAGE, null, skillOptions, skillOptions[0]);
			if(skill == null)
				skill = "Done";
		}

		acrobatics = dexmod;
		arcana = intmod;
		athletics = strmod;
		bluff = chamod;
		diplomacy = chamod;
		dungeoneering = wismod;
		endurance = conmod;
		heal = wismod;
		history = intmod;
		insight = wismod;
		intimidate = chamod;
		nature = wismod;
		perception = wismod;
		religion = intmod;
		stealth = dexmod;
		streetwise = chamod;
		thievery = dexmod;
		initiative = dexmod;

		for(String s : trained_skills)
		{
			if(s.equals("Acrobatics"))
				acrobatics += 5;
			else if(s.equals("Arcana"))
				arcana += 5;
			else if(s.equals("Athletics"))
				athletics += 5;
			else if(s.equals("Bluff"))
				bluff += 5;
			else if(s.equals("Diplomacy"))
				diplomacy += 5;
			else if(s.equals("Dungeoneering"))
				dungeoneering += 5;
			else if(s.equals("Endurance"))
				endurance += 5;
			else if(s.equals("Heal"))
				heal += 5;
			else if(s.equals("History"))
				history += 5;
			else if(s.equals("Insight"))
				insight += 5;
			else if(s.equals("Intimidate"))
				intimidate += 5;
			else if(s.equals("Nature"))
				nature += 5;
			else if(s.equals("Perception"))
				perception += 5;
			else if(s.equals("Religion"))
				religion += 5;
			else if(s.equals("Stealth"))
				stealth += 5;
			else if(s.equals("Streetwise"))
				streetwise += 5;
			else if(s.equals("Thievery"))
				thievery += 5;
		}

		if(role.equals("Skirmisher"))
		{
			initiative += 2;
			health = 8 + constitution + level*8;
			ac = level + 14;
			fort = level + 12;
			ref = level + 12;
			will = level + 12;
			attack_vs_ac = level + 5;
			attack_vs_other = level + 3;
			attack_vs_multiple = level + 1;
		}
		else if(role.equals("Brute"))
		{
			initiative += 0;
			health = 10 + constitution + level*10;
			ac = level + 12;
			fort = level + 12;
			ref = level + 12;
			will = level + 12;
			attack_vs_ac = level + 3;
			attack_vs_other = level + 1;
			attack_vs_multiple = level - 1;
		}
		else if(role.equals("Soldier"))
		{
			initiative += 2;
			health = 8 + constitution + level*8;
			ac = level + 16;
			fort = level + 12;
			ref = level + 12;
			will = level + 12;
			attack_vs_ac = level + 7;
			attack_vs_other = level + 5;
			attack_vs_multiple = level + 3;
		}
		else if(role.equals("Lurker"))
		{
			initiative += 4;
			health = 6 + constitution + level*6;
			ac = level + 14;
			fort = level + 12;
			ref = level + 12;
			will = level + 12;
			attack_vs_ac = level + 5;
			attack_vs_other = level + 3;
			attack_vs_multiple = level + 1;
		}
		else if(role.equals("Controller"))
		{
			initiative += 0;
			health = 8 + constitution + level*8;
			ac = level + 14;
			fort = level + 12;
			ref = level + 12;
			will = level + 12;
			attack_vs_ac = level + 5;
			attack_vs_other = level + 4;
			attack_vs_multiple = level + 2;
		}
		else
		{
			initiative += 0;
			health = 6 + constitution + level*6;
			ac = level + 12;
			fort = level + 12;
			ref = level + 12;
			will = level + 12;
			attack_vs_ac = level + 7;
			attack_vs_other = level + 5;
			attack_vs_multiple = level + 3;
		}

		if(epic_mode)
			health /= 2;

		if(main_attribute.equals("Strength"))
			fort += 1;
		else if(main_attribute.equals("Constitution"))
			fort += 1;
		else if(main_attribute.equals("Dexterity"))
			ref += 1;
		else if(main_attribute.equals("Intelligence"))
			ref += 1;
		else if(main_attribute.equals("Wisdom"))
			will += 1;
		else
			will += 1;

		done_creating = true;
	}

	private void createPowers()
	{
		Object[] type = {"Attack Power", "Other Power"};
		Object chosenType;
		chosenType = JOptionPane.showInputDialog(null, "What kind of power is it?", "Creating powers...", JOptionPane.QUESTION_MESSAGE, null, type, type[0]);
		if(chosenType == null)
		{
			chosenType = type[0];
		}
		if(chosenType.equals("Attack Power"))
			createPower();
		else
			createOtherPower();
	}

	private void createOtherPower()
	{
		String power_name, action_type, power_type, recharge, description;

		power_name = (String)JOptionPane.showInputDialog(null, "Name the power", "Power", JOptionPane.QUESTION_MESSAGE);
		if(power_name == null)
			power_name = "Unnamed";

		Object[] actionTypes = {Power.STANDARD, Power.MOVE, Power.MINOR, Power.IMMEDIATE};
		action_type = (String)JOptionPane.showInputDialog(null, "Select the type of action", power_name, JOptionPane.QUESTION_MESSAGE, null, actionTypes, actionTypes[0]);
		if(action_type == null)
			action_type = (String)actionTypes[0];

		Object[] powerTypes = {Power.AT_WILL, Power.ENCOUNTER, Power.RECHARGE, Power.DAILY};
		power_type = (String)JOptionPane.showInputDialog(null, "Select the type of power", power_name, JOptionPane.QUESTION_MESSAGE, null, powerTypes, powerTypes[0]);
		if(power_type == null)
			power_type = (String)powerTypes[0];

		if(power_type.equals(Power.RECHARGE))
		{
			Object[] recharges = {"1+", "2+", "3+", "4+", "5+", "6"};
			recharge = (String)JOptionPane.showInputDialog(null, "Select recharge threshold", power_name, JOptionPane.QUESTION_MESSAGE, null, recharges, recharges[0]);
			if(recharge == null)
				recharge = (String)recharges[0];
		}
		else
		{
			recharge = null;
		}

		description = (String)JOptionPane.showInputDialog(null, "Describe the power", power_name, JOptionPane.QUESTION_MESSAGE);
		if(description == null)
			description = "";

		other_powers.add(new Other_Power(power_name, action_type, power_type, recharge, description));
	}

	private void createPower()
	{
		/*
		 *Name
		 *Attack type
		 *range
		 *damage type
		 *defence
		 *number of dice
		 *die
		 *action type
		 *power type
		 */
		int attack, number_of_dice, range, area, damage_mod;
		String power_name, attack_type, damage_type, defence, die, action_type, power_type, recharge, description;

		power_name = (String)JOptionPane.showInputDialog(null, "Name the power", "Power", JOptionPane.QUESTION_MESSAGE);
		if(power_name == null)
			power_name = "Unnamed";

		Object[] attackOptions = {
								Power.BURST,
								Power.BLAST,
								Power.WALL,
								Power.CLOSE_BURST,
								Power.CLOSE_BLAST,
								Power.MELEE,
								Power.MELEE_TOUCH,
								Power.RANGED
								};
		attack_type = (String)JOptionPane.showInputDialog(null, "Select the type of attack", power_name, JOptionPane.QUESTION_MESSAGE, null, attackOptions, attackOptions[0]);
		if(attack_type == null)
			attack_type = (String)attackOptions[0];

		if(attack_type.equals(Power.BURST) || attack_type.equals(Power.BLAST) || attack_type.equals(Power.CLOSE_BURST) || attack_type.equals(Power.CLOSE_BLAST))
		{
			String tmp = "";
			while(!isInteger(tmp))
				tmp = JOptionPane.showInputDialog(null, "Enter area size", power_name, JOptionPane.QUESTION_MESSAGE);
			area = Integer.parseInt(tmp);
		}
		else
			area = 0;

		String in = "";
		if(attack_type.equals(Power.CLOSE_BURST) || attack_type.equals(Power.CLOSE_BLAST))
			range = 0;
		else
		{
			while(!isInteger(in))
			in = JOptionPane.showInputDialog(null, "Enter range", power_name, JOptionPane.QUESTION_MESSAGE);
			range = Integer.parseInt(in);
		}

		Object[] damageOptions = {
								Power.NORMAL,
								Power.FIRE,
								Power.COLD,
								Power.RADIANT,
								Power.NECROTIC,
								Power.ACID,
								Power.PSYCHIC,
								Power.LIGHTNING,
								Power.THUNDER,
								Power.FORCE,
								Power.POISON
								};
		damage_type = (String)JOptionPane.showInputDialog(null, "Select the type of damage", power_name, JOptionPane.QUESTION_MESSAGE, null, damageOptions, damageOptions[0]);
		if(damage_type == null)
			damage_type = (String)damageOptions[0];

		Object[] defences = {
							Power.AC,
							Power.FORTITUDE,
							Power.REFLEX,
							Power.WILL
							};
		defence = (String)JOptionPane.showInputDialog(null, "Select the target defence", power_name, JOptionPane.QUESTION_MESSAGE, null, defences, defences[0]);
		if(defence == null)
			defence = (String)defences[0];

		if(!attack_type.equals(Power.MELEE) && !attack_type.equals(Power.MELEE_TOUCH) && !attack_type.equals(Power.RANGED))
			attack = attack_vs_multiple;
		else
			if(defence.equals(Power.AC))
				attack = attack_vs_ac;
			else
				attack = attack_vs_other;

		in = "";
		while(!isInteger(in))
			in = JOptionPane.showInputDialog(null, "Enter number of damage dice rolled", power_name, JOptionPane.QUESTION_MESSAGE);
		number_of_dice = Integer.parseInt(in);

		Object[] dice = {
						"d4", "d6", "d8", "d10", "d12"
						};

		die = (String)JOptionPane.showInputDialog(null, "Select the type of damage die", power_name, JOptionPane.QUESTION_MESSAGE, null, dice, dice[0]);
		if(die == null)
			die = (String)dice[0];

		in = "";
		while(!isInteger(in))
			in = JOptionPane.showInputDialog(null, "Enter damage modifier", power_name, JOptionPane.QUESTION_MESSAGE);
		damage_mod = Integer.parseInt(in);

		Object[] actionTypes = {Power.STANDARD, Power.MOVE, Power.MINOR, Power.IMMEDIATE};
		action_type = (String)JOptionPane.showInputDialog(null, "Select the type of action", power_name, JOptionPane.QUESTION_MESSAGE, null, actionTypes, actionTypes[0]);
		if(action_type == null)
			action_type = (String)actionTypes[0];

		Object[] powerTypes = {Power.AT_WILL, Power.ENCOUNTER, Power.RECHARGE, Power.DAILY};
		power_type = (String)JOptionPane.showInputDialog(null, "Select the type of power", power_name, JOptionPane.QUESTION_MESSAGE, null, powerTypes, powerTypes[0]);
		if(power_type == null)
			power_type = (String)powerTypes[0];

		if(power_type.equals(Power.RECHARGE))
		{
			Object[] recharges = {"1+", "2+", "3+", "4+", "5+", "6"};
			recharge = (String)JOptionPane.showInputDialog(null, "Select recharge threshold", power_name, JOptionPane.QUESTION_MESSAGE, null, recharges, recharges[0]);
			if(recharge == null)
				recharge = (String)recharges[0];
		}
		else
		{
			recharge = null;
		}

		description = (String)JOptionPane.showInputDialog(null, "Describe the power", power_name, JOptionPane.QUESTION_MESSAGE);
		if(description == null)
			description = "";

		powers.add(new Power(power_name, attack_type, damage_type, attack, area, range, defence, number_of_dice, die, damage_mod, action_type, power_type, recharge, description));
	}

	private void setAverages()
	{
		ascore = 13+level/2;
		if(main1.equals("Strength"))
		{
			astrength = 13+level/2;
			aconstitution = 10;
		}
		else
		{
			aconstitution = 13+level/2;
			astrength = 10;
		}
		if(main2.equals("Dexterity"))
		{
			adexterity = 13+level/2;
			aintelligence = 10;
		}
		else
		{
			aintelligence = 13+level/2;
			adexterity = 10;
		}
		if(main3.equals("Wisdom"))
		{
			awisdom = 13+level/2;
			acharisma = 10;
		}
		else
		{
			acharisma = 13+level/2;
			awisdom = 10;
		}

		strength = astrength;
		constitution = aconstitution;
		dexterity = adexterity;
		intelligence = aintelligence;
		wisdom = awisdom;
		charisma = acharisma;

//		if(main_attribute.equals("Strength"))
//			strength += 3;
//		else if(main_attribute.equals("Constitution"))
//			constitution += 3;
//		else if(main_attribute.equals("Dexterity"))
//			dexterity += 3;
//		else if(main_attribute.equals("Intelligence"))
//			intelligence += 3;
//		else if(main_attribute.equals("Wisdom"))
//			wisdom += 3;
//		else
//			charisma += 3;
	}

	private void calculate()
	{
		xp = experience();

		if(level_changed)
		{
			if(main_attribute.equals("Strength"))
				strength += 3;
			else if(main_attribute.equals("Constitution"))
				constitution += 3;
			else if(main_attribute.equals("Dexterity"))
				dexterity += 3;
			else if(main_attribute.equals("Intelligence"))
				intelligence += 3;
			else if(main_attribute.equals("Wisdom"))
				wisdom += 3;
			else
				charisma += 3;
			level_changed = false;
		}

		strmod = strength/2-5+level/2;
		conmod = constitution/2-5+level/2;
		dexmod = dexterity/2-5+level/2;
		intmod = intelligence/2-5+level/2;
		wismod = wisdom/2-5+level/2;
		chamod = charisma/2-5+level/2;

		acrobatics = dexmod;
		arcana = intmod;
		athletics = strmod;
		bluff = chamod;
		diplomacy = chamod;
		dungeoneering = wismod;
		endurance = conmod;
		heal = wismod;
		history = intmod;
		insight = wismod;
		intimidate = chamod;
		nature = wismod;
		perception = wismod;
		religion = intmod;
		stealth = dexmod;
		streetwise = chamod;
		thievery = dexmod;
		initiative = dexmod;

		for(String s : trained_skills)
		{
			if(s.equals("Acrobatics"))
				acrobatics += 5;
			else if(s.equals("Arcana"))
				arcana += 5;
			else if(s.equals("Athletics"))
				athletics += 5;
			else if(s.equals("Bluff"))
				bluff += 5;
			else if(s.equals("Diplomacy"))
				diplomacy += 5;
			else if(s.equals("Dungeoneering"))
				dungeoneering += 5;
			else if(s.equals("Endurance"))
				endurance += 5;
			else if(s.equals("Heal"))
				heal += 5;
			else if(s.equals("History"))
				history += 5;
			else if(s.equals("Insight"))
				insight += 5;
			else if(s.equals("Intimidate"))
				intimidate += 5;
			else if(s.equals("Nature"))
				nature += 5;
			else if(s.equals("Perception"))
				perception += 5;
			else if(s.equals("Religion"))
				religion += 5;
			else if(s.equals("Stealth"))
				stealth += 5;
			else if(s.equals("Streetwise"))
				streetwise += 5;
			else if(s.equals("Thievery"))
				thievery += 5;
		}

		if(role.equals("Skirmisher"))
		{
			initiative += 2;
			health = 8 + constitution + level*8;
			ac = level + 14;
			fort = level + 12;
			ref = level + 12;
			will = level + 12;
			attack_vs_ac = level + 5;
			attack_vs_other = level + 3;
			attack_vs_multiple = level + 1;
		}
		else if(role.equals("Brute"))
		{
			initiative += 0;
			health = 10 + constitution + level*10;
			ac = level + 12;
			fort = level + 12;
			ref = level + 12;
			will = level + 12;
			attack_vs_ac = level + 3;
			attack_vs_other = level + 1;
			attack_vs_multiple = level - 1;
		}
		else if(role.equals("Soldier"))
		{
			initiative += 2;
			health = 8 + constitution + level*8;
			ac = level + 16;
			fort = level + 12;
			ref = level + 12;
			will = level + 12;
			attack_vs_ac = level + 7;
			attack_vs_other = level + 5;
			attack_vs_multiple = level + 3;
		}
		else if(role.equals("Lurker"))
		{
			initiative += 4;
			health = 6 + constitution + level*6;
			ac = level + 14;
			fort = level + 12;
			ref = level + 12;
			will = level + 12;
			attack_vs_ac = level + 5;
			attack_vs_other = level + 3;
			attack_vs_multiple = level + 1;
		}
		else if(role.equals("Controller"))
		{
			initiative += 0;
			health = 8 + constitution + level*8;
			ac = level + 14;
			fort = level + 12;
			ref = level + 12;
			will = level + 12;
			attack_vs_ac = level + 5;
			attack_vs_other = level + 4;
			attack_vs_multiple = level + 2;
		}
		else
		{
			initiative += 0;
			health = 6 + constitution + level*6;
			ac = level + 12;
			fort = level + 12;
			ref = level + 12;
			will = level + 12;
			attack_vs_ac = level + 7;
			attack_vs_other = level + 5;
			attack_vs_multiple = level + 3;
		}


	}

	private void editMonster()
	{
		level_changed = false;
		Object editChoice = "";
		if(type.equals("Normal"))
		{
			Object[]editOptions = {"Name", "Level", "Role", "Speed", "Ability Scores", "Vision", "Trained Skills", "Powers", "Make Minion", "Make Elite", "Make Solo"};
			editChoice = JOptionPane.showInputDialog(null, "Select attribute to edit", name, JOptionPane.QUESTION_MESSAGE, null, editOptions, editOptions[0]);
			if(editChoice == null)
				editChoice = "";
		}
		else
		{
			Object[] editOptions = {"Name", "Level", "Role", "Speed", "Ability Scores", "Vision", "Trained Skills", "Powers", "Make Normal"};
			editChoice = JOptionPane.showInputDialog(null, "Select attribute to edit", name, JOptionPane.QUESTION_MESSAGE, null, editOptions, editOptions[0]);
			if(editChoice == null)
				editChoice = "";
		}

		if(editChoice.equals("Name"))
		{
			old_name = name;
			name_changed = true;
			name = (String)JOptionPane.showInputDialog(null, "Enter new name", name, JOptionPane.QUESTION_MESSAGE);
			if(name == null)
				name = old_name;
		}
		else if(editChoice.equals("Level"))
		{
			level_changed = true;
			String in = "";
			while(!isInteger(in))
				in = JOptionPane.showInputDialog(null, "Enter monster level", name, JOptionPane.QUESTION_MESSAGE);
			level = Integer.parseInt(in);
			setAverages();
			calculate();
			if(strength > constitution)
			{
				int adjust = (strength-ascore)/2;
				fort += adjust;
			}
			else
			{
				int adjust = (constitution-ascore)/2;
				fort += adjust;
			}
			if(dexterity > intelligence)
			{
				int adjust = (dexterity-ascore)/2;
				ref += adjust;
			}
			else
			{
				int adjust = (intelligence-ascore)/2;
				ref += adjust;
			}
			if(wisdom > charisma)
			{
				int adjust = (wisdom-ascore)/2;
				will += adjust;
			}
			else
			{
				int adjust = (charisma-ascore)/2;
				will += adjust;
			}
		}
		else if(editChoice.equals("Role"))
		{
			Object[] roleOptions = {
								"Skirmisher",
								"Brute",
								"Soldier",
								"Lurker",
								"Controller",
								"Artillery"
								};
			role = JOptionPane.showInputDialog(null, "Select a role", name, JOptionPane.QUESTION_MESSAGE, null, roleOptions, roleOptions[0]);
			if(role == null)
			{
				role = roleOptions[0];
			}
			calculate();
		}
		else if(editChoice.equals("Speed"))
		{
			String in = "";
			while(!isInteger(in))
				in = JOptionPane.showInputDialog(null, "Enter monster speed", name, JOptionPane.QUESTION_MESSAGE);
			speed = Integer.parseInt(in);
		}
		else if(editChoice.equals("Ability Scores"))
		{
			Object[] abilities = {"Strength", "Constitution", "Dexterity", "Intelligence", "Wisdom", "Charisma"};
			Object editAbility;
			editAbility = JOptionPane.showInputDialog(null, "Which ability?", name, JOptionPane.QUESTION_MESSAGE, null, abilities, abilities[0]);
			if(editAbility == null)
			{
				editAbility = abilities[0];
			}
			if(editAbility.equals("Strength"))
			{
				String in = "";
				while(!isInteger(in))
					in = JOptionPane.showInputDialog(null, "Enter new strength score", name, JOptionPane.QUESTION_MESSAGE);
				strength = Integer.parseInt(in);
				calculate();
				if(strength > constitution)
				{
					int adjust = (strength-ascore)/2;
					fort += adjust;
				}
				else
				{
					int adjust = (constitution-ascore)/2;
					fort += adjust;
				}
			}
			else if(editAbility.equals("Constitution"))
			{
				String in = "";
				while(!isInteger(in))
					in = JOptionPane.showInputDialog(null, "Enter new constitution score", name, JOptionPane.QUESTION_MESSAGE);
				constitution = Integer.parseInt(in);
				calculate();
				if(strength > constitution)
				{
					int adjust = (strength-ascore)/2;
					fort += adjust;
				}
				else
				{
					int adjust = (constitution-ascore)/2;
					fort += adjust;
				}
			}
			else if(editAbility.equals("Dexterity"))
			{
				String in = "";
				while(!isInteger(in))
					in = JOptionPane.showInputDialog(null, "Enter new dexterity score", name, JOptionPane.QUESTION_MESSAGE);
				dexterity = Integer.parseInt(in);
				calculate();
				if(dexterity > intelligence)
				{
					int adjust = (dexterity-ascore)/2;
					ref += adjust;
				}
				else
				{
					int adjust = (intelligence-ascore)/2;
					ref += adjust;
				}
			}
			else if(editAbility.equals("Intelligence"))
			{
				String in = "";
				while(!isInteger(in))
					in = JOptionPane.showInputDialog(null, "Enter new intelligence score", name, JOptionPane.QUESTION_MESSAGE);
				intelligence = Integer.parseInt(in);
				calculate();
				if(dexterity > intelligence)
				{
					int adjust = (dexterity-ascore)/2;
					ref += adjust;
				}
				else
				{
					int adjust = (intelligence-ascore)/2;
					ref += adjust;
				}
			}
			else if(editAbility.equals("Wisdom"))
			{
				String in = "";
				while(!isInteger(in))
					in = JOptionPane.showInputDialog(null, "Enter new wisdom score", name, JOptionPane.QUESTION_MESSAGE);
				wisdom = Integer.parseInt(in);
				calculate();
				if(wisdom > charisma)
				{
					int adjust = (wisdom-ascore)/2;
					will += adjust;
				}
				else
				{
					int adjust = (charisma-ascore)/2;
					will += adjust;
				}
			}
			else if(editAbility.equals("Charisma"))
			{
				String in = "";
				while(!isInteger(in))
					in = JOptionPane.showInputDialog(null, "Enter new charisma score", name, JOptionPane.QUESTION_MESSAGE);
				charisma = Integer.parseInt(in);
				calculate();
				if(wisdom > charisma)
				{
					int adjust = (wisdom-ascore)/2;
					will += adjust;
				}
				else
				{
					int adjust = (charisma-ascore)/2;
					will += adjust;
				}
			}
		}
		else if(editChoice.equals("Vision"))
		{
			Object[] visionOptions = {
						"Darkvision",
						"Low-light Vision",
						"Normal"
						};
			Object vision_select;
			vision_select = JOptionPane.showInputDialog(null, "Select the level of vision", name, JOptionPane.QUESTION_MESSAGE, null, visionOptions, visionOptions[0]);
			if(vision_select == null)
			{
				vision_select = visionOptions[0];
			}

			vision = (String)vision_select;
		}
		else if(editChoice.equals("Trained Skills"))
		{
			trained_skills.clear();
			Object[] skillOptions = {
							"Done",
							"Acrobatics",
							"Arcana",
							"Athletics",
							"Bluff",
							"Diplomacy",
							"Dungeoneering",
							"Endurance",
							"Heal",
							"History",
							"Insight",
							"Intimidate",
							"Nature",
							"Perception",
							"Religion",
							"Stealth",
							"Streetwise",
							"Thievery",
							};

			String skill = (String)JOptionPane.showInputDialog(null, "Select a trained skill, or choose 'Done'", name, JOptionPane.QUESTION_MESSAGE, null, skillOptions, skillOptions[0]);
			while(!skill.equals("Done"))
			{
				trained_skills.add(skill);
				skill = (String)JOptionPane.showInputDialog(null, "Select a trained skill, or choose 'Done'", name, JOptionPane.QUESTION_MESSAGE, null, skillOptions, skillOptions[0]);
			}
			calculate();
		}
		else if(editChoice.equals("Powers"))
		{
			Power editedPower = null;
			Other_Power editedOtherPower = null;
			ArrayList<String> power_names = new ArrayList<String>();
			for(int i = 0; i < powers.size(); i++)
				power_names.add(powers.get(i).getName());
			for(int i = 0; i < other_powers.size(); i++)
				power_names.add(other_powers.get(i).getName());
			Object[] powerOptions = power_names.toArray();

			Object chosenPower;
			chosenPower = JOptionPane.showInputDialog(null, "Select a power to edit", "Powers", JOptionPane.QUESTION_MESSAGE, null, powerOptions, powerOptions[0]);
			if(chosenPower == null)
			{
				chosenPower = powerOptions[0];
			}
			int index_of_powers;
			for(index_of_powers = 0; index_of_powers < powers.size(); index_of_powers++)
			{
				if(powers.get(index_of_powers).getName().equals(chosenPower))
				{
					editedPower = editPower(powers.get(index_of_powers));
					break;
				}
			}
			int index_of_other_powers;
			for(index_of_other_powers = 0; index_of_other_powers < other_powers.size(); index_of_other_powers++)
			{
				if(other_powers.get(index_of_other_powers).getName().equals(chosenPower))
				{
					editedOtherPower = editOtherPower(other_powers.get(index_of_other_powers));
					break;
				}
			}

			if(editedPower != null)
			{
				powers.remove(index_of_powers);
				powers.add(index_of_powers, editedPower);
			}
			else if(editedOtherPower != null)
			{
				other_powers.remove(index_of_other_powers);
				other_powers.add(index_of_other_powers, editedOtherPower);
			}
		}
		else if(editChoice.equals("Make Minion"))
		{
			xp = experience()/4;
			health = 1;
			type = "Minion";
		}
		else if(editChoice.equals("Make Elite"))
		{
			xp = experience()*2;
			if(epic_mode)
				health = health*2 + constitution;
			else
				health = health*2 + constitution*2;
			type = "Elite";
			Object[] defenceOptions = {"Done", Power.AC, Power.FORTITUDE, Power.REFLEX, Power.WILL};
			Object chosenDefence;
			for(int i = 0; i < 3; i++)
			{
				chosenDefence = "Done";
				chosenDefence = JOptionPane.showInputDialog(null, "Select a defence to increase", "Defences", JOptionPane.QUESTION_MESSAGE, null, defenceOptions, defenceOptions[0]);
				if(chosenDefence == null)
					chosenDefence = "Done";
				if(chosenDefence.equals("Done"))
					break;
				else
					if(chosenDefence.equals(Power.AC))
						ac += 2;
					else if(chosenDefence.equals(Power.FORTITUDE))
						fort += 2;
					else if(chosenDefence.equals(Power.REFLEX))
						ref += 2;
					else if(chosenDefence.equals(Power.WILL))
						will += 2;
			}
			other_powers.add(new Other_Power("Resilience", Power.IMMEDIATE, Power.AT_WILL, "", "+2 to saving throws"));
			JOptionPane.showMessageDialog(null, "Make an encounter power recharge when the monster is first bloodied\n"
												+ "Add an immediate interrupt power OR create a way to give the monster an extra attack on its turn\n"
												+ "Elite monsters have 1 action point",
												"Reminder", JOptionPane.ERROR_MESSAGE);
		}
		else if(editChoice.equals("Make Solo"))
		{
			xp = experience()*5;
			health = 8*(level+1)+constitution;
			if(level <= 10)
				health *= 4;
			else
				health *= 5;
			type = "Solo";
			if(epic_mode)
				health /= 2;
			Object[] defenceOptions = {"Done", Power.AC, Power.FORTITUDE, Power.REFLEX, Power.WILL};
			Object chosenDefence;
			for(int i = 0; i < 3; i++)
			{
				chosenDefence = "Done";
				chosenDefence = JOptionPane.showInputDialog(null, "Select a defence to increase", "Defences", JOptionPane.QUESTION_MESSAGE, null, defenceOptions, defenceOptions[0]);
				if(chosenDefence == null)
					chosenDefence = "Done";
				if(chosenDefence.equals("Done"))
					break;
				else
					if(chosenDefence.equals(Power.AC))
						ac += 2;
					else if(chosenDefence.equals(Power.FORTITUDE))
						fort += 2;
					else if(chosenDefence.equals(Power.REFLEX))
						ref += 2;
					else if(chosenDefence.equals(Power.WILL))
						will += 2;
			}

			other_powers.add(new Other_Power("Resilience", Power.IMMEDIATE, Power.AT_WILL, "", "+5 to saving throws"));

			JOptionPane.showMessageDialog(null, "Make an encounter power an at-will power\n"
												+ "Solo monsters have an additional standard action\n"
												+ "Solo monsters have 2 action points",
												"Reminder", JOptionPane.ERROR_MESSAGE);
		}
		else if(editChoice.equals("Make Normal"))
		{
			type = "Normal";
			calculate();
			ascore = 13+level/2;
			if(strength > constitution)
			{
				int adjust = (strength-ascore)/2;
				fort += adjust;
			}
			else
			{
				int adjust = (constitution-ascore)/2;
				fort += adjust;
			}
			if(dexterity > intelligence)
			{
				int adjust = (dexterity-ascore)/2;
				ref += adjust;
			}
			else
			{
				int adjust = (intelligence-ascore)/2;
				ref += adjust;
			}
			if(wisdom > charisma)
			{
				int adjust = (wisdom-ascore)/2;
				will += adjust;
			}
			else
			{
				int adjust = (charisma-ascore)/2;
				will += adjust;
			}
		}
	}

	private Power editPower(Power p)
	{
		/*
		 *Name
		 *Attack type
		 *area
		 *range
		 *damage type
		 *defence
		 *number of dice
		 *die
		 *dmg mod
		 *action type
		 *power type
		 *recharge
		 *description
		 */
		Object chosenEdit = null;
		if(p.getAttackType().equals(Power.BURST) || p.getAttackType().equals(Power.BLAST) || p.getAttackType().equals(Power.CLOSE_BURST) || p.getAttackType().equals(Power.CLOSE_BLAST))
		{
			if(p.getPowerType().equals(Power.RECHARGE))
			{
				if(p.getAttackType().equals(Power.CLOSE_BURST) || p.getAttackType().equals(Power.CLOSE_BLAST))
				{
					Object[] powerOptions = {
									"Name",
									"Attack Type",
									"Area",
									"Damage Type",
									"Defence",
									"Number of Damage Dice",
									"Damage Die",
									"Damage Modifier",
									"Action Type",
									"Power Type",
									"Recharge",
									"Description"
									};
					chosenEdit = JOptionPane.showInputDialog(null, "What would you like to edit?", name, JOptionPane.QUESTION_MESSAGE, null, powerOptions, powerOptions[0]);
					if(chosenEdit == null)
						chosenEdit = "";
				}
				else
				{
					Object[] powerOptions = {
									"Name",
									"Attack Type",
									"Area",
									"Range",
									"Damage Type",
									"Defence",
									"Number of Damage Dice",
									"Damage Die",
									"Damage Modifier",
									"Action Type",
									"Power Type",
									"Recharge",
									"Description"
									};
					chosenEdit = JOptionPane.showInputDialog(null, "What would you like to edit?", name, JOptionPane.QUESTION_MESSAGE, null, powerOptions, powerOptions[0]);
					if(chosenEdit == null)
						chosenEdit = "";
				}
			}
			else
			{
				Object[] powerOptions = {
								"Name",
								"Attack Type",
								"Area",
								"Range",
								"Damage Type",
								"Defence",
								"Number of Damage Dice",
								"Damage Die",
								"Damage Modifier",
								"Action Type",
								"Power Type",
								"Description"
								};
					chosenEdit = JOptionPane.showInputDialog(null, "What would you like to edit?", name, JOptionPane.QUESTION_MESSAGE, null, powerOptions, powerOptions[0]);
					if(chosenEdit == null)
						chosenEdit = "";
			}
		}
		else
		{
			if(p.getPowerType().equals(Power.RECHARGE))
			{
				Object[] powerOptions = {
								"Name",
								"Attack Type",
								"Range",
								"Damage Type",
								"Defence",
								"Number of Damage Dice",
								"Damage Die",
								"Damage Modifier",
								"Action Type",
								"Power Type",
								"Recharge",
								"Description"
								};
					chosenEdit = JOptionPane.showInputDialog(null, "What would you like to edit?", name, JOptionPane.QUESTION_MESSAGE, null, powerOptions, powerOptions[0]);
					if(chosenEdit == null)
						chosenEdit = "";
			}
			else
			{
				Object[] powerOptions = {
								"Name",
								"Attack Type",
								"Range",
								"Damage Type",
								"Defence",
								"Number of Damage Dice",
								"Damage Die",
								"Damage Modifier",
								"Action Type",
								"Power Type",
								"Description"
								};
					chosenEdit = JOptionPane.showInputDialog(null, "What would you like to edit?", name, JOptionPane.QUESTION_MESSAGE, null, powerOptions, powerOptions[0]);
					if(chosenEdit == null)
						chosenEdit = "";
			}
		}

		int attack;

		String name = p.getName();
		String attack_type = p.getAttackType();
		int area = p.getArea();
		int range = p.getRange();
		String damage_type = p.getDamageType();
		String defence = p.getDefence();
		int num_dice = p.getNumberOfDice();
		String die = p.getDie();
		int damage_mod = p.getDamageMod();
		String action_type = p.getActionType();
		String power_type = p.getPowerType();
		String recharge = p.getRecharge();
		String description = p.getDescription();

		if(chosenEdit.equals("Name"))
		{
			name = JOptionPane.showInputDialog(null, "Enter new name", "Renaming...", JOptionPane.QUESTION_MESSAGE);
			if(name == null)
				name = "Unnamed";
		}
		else if(chosenEdit.equals("Attack Type"))
		{
			Object[] attackOptions = {
								Power.BURST,
								Power.BLAST,
								Power.WALL,
								Power.CLOSE_BURST,
								Power.CLOSE_BLAST,
								Power.MELEE,
								Power.MELEE_TOUCH,
								Power.RANGED
								};
			attack_type = (String)JOptionPane.showInputDialog(null, "Select the type of attack", name, JOptionPane.QUESTION_MESSAGE, null, attackOptions, attackOptions[0]);

			if(attack_type.equals(Power.BURST) || attack_type.equals(Power.BLAST) || attack_type.equals(Power.CLOSE_BURST) || attack_type.equals(Power.CLOSE_BLAST))
			{
				String tmp = "";
				while(!isInteger(tmp))
					tmp = JOptionPane.showInputDialog(null, "Enter area size", name, JOptionPane.QUESTION_MESSAGE);
				area = Integer.parseInt(tmp);
			}
			else
				area = 0;

			String in = "";
			if(attack_type.equals(Power.CLOSE_BURST) || attack_type.equals(Power.CLOSE_BLAST))
				range = 0;
			else
			{
				while(!isInteger(in))
					in = JOptionPane.showInputDialog(null, "Enter range", name, JOptionPane.QUESTION_MESSAGE);
				range = Integer.parseInt(in);
			}
		}
		else if(chosenEdit.equals("Area"))
		{
			String in = "";
			while(!isInteger(in))
					in = JOptionPane.showInputDialog(null, "Enter area", name, JOptionPane.QUESTION_MESSAGE);
			area = Integer.parseInt(in);
		}
		else if(chosenEdit.equals("Range"))
		{
			String in = "";
			while(!isInteger(in))
					in = JOptionPane.showInputDialog(null, "Enter range", name, JOptionPane.QUESTION_MESSAGE);
			range = Integer.parseInt(in);
		}
		else if(chosenEdit.equals("Damage Type"))
		{
			Object[] damageOptions = {
								Power.NORMAL,
								Power.FIRE,
								Power.COLD,
								Power.RADIANT,
								Power.NECROTIC,
								Power.ACID,
								Power.PSYCHIC,
								Power.LIGHTNING,
								Power.THUNDER,
								Power.FORCE,
								Power.POISON
								};
			damage_type = (String)JOptionPane.showInputDialog(null, "Select the type of damage", name, JOptionPane.QUESTION_MESSAGE, null, damageOptions, damageOptions[0]);
		}
		else if(chosenEdit.equals("Defence"))
		{
			Object[] defences = {
							Power.AC,
							Power.FORTITUDE,
							Power.REFLEX,
							Power.WILL
							};
			defence = (String)JOptionPane.showInputDialog(null, "Select the target defence", name, JOptionPane.QUESTION_MESSAGE, null, defences, defences[0]);
		}
		else if(chosenEdit.equals("Number of Damage Dice"))
		{
			String in = "";
			while(!isInteger(in))
					in = JOptionPane.showInputDialog(null, "Enter number of damage dice", name, JOptionPane.QUESTION_MESSAGE);
			num_dice = Integer.parseInt(in);
		}
		else if(chosenEdit.equals("Damage Die"))
		{
			Object[] dice = {
						"d4", "d6", "d8", "d10", "d12"
						};

			die = (String)JOptionPane.showInputDialog(null, "Select the type of damage die", name, JOptionPane.QUESTION_MESSAGE, null, dice, dice[0]);
		}
		else if(chosenEdit.equals("Damage Modifier"))
		{
			String in = "";
			while(!isInteger(in))
					in = JOptionPane.showInputDialog(null, "Enter damage modifier", name, JOptionPane.QUESTION_MESSAGE);
			damage_mod = Integer.parseInt(in);
		}
		else if(chosenEdit.equals("Action Type"))
		{
			Object[] actionTypes = {Power.STANDARD, Power.MOVE, Power.MINOR, Power.IMMEDIATE};
			action_type = (String)JOptionPane.showInputDialog(null, "Select the type of action", name, JOptionPane.QUESTION_MESSAGE, null, actionTypes, actionTypes[0]);
		}
		else if(chosenEdit.equals("Power Type"))
		{
			Object[] powerTypes = {Power.AT_WILL, Power.ENCOUNTER, Power.RECHARGE, Power.DAILY};
			power_type = (String)JOptionPane.showInputDialog(null, "Select the type of power", name, JOptionPane.QUESTION_MESSAGE, null, powerTypes, powerTypes[0]);
			if(power_type.equals("Recharge"))
			{
				Object[] recharges = {"1+", "2+", "3+", "4+", "5+", "6"};
				recharge = (String)JOptionPane.showInputDialog(null, "Select recharge threshold", name, JOptionPane.QUESTION_MESSAGE, null, recharges, recharges[0]);
			}
			else
				recharge = null;
		}
		else if(chosenEdit.equals("Recharge"))
		{
			Object[] recharges = {"1+", "2+", "3+", "4+", "5+", "6"};
			recharge = (String)JOptionPane.showInputDialog(null, "Select recharge threshold", name, JOptionPane.QUESTION_MESSAGE, null, recharges, recharges[0]);
		}
		else if(chosenEdit.equals("Description"))
		{
			String old_description = description;
			description = JOptionPane.showInputDialog(null, "Describe the power", name, JOptionPane.QUESTION_MESSAGE);
			if(description == null)
				description = old_description;
		}

		if(!attack_type.equals(Power.MELEE) && !attack_type.equals(Power.MELEE_TOUCH) && !attack_type.equals(Power.RANGED))
			attack = attack_vs_multiple;
		else
			if(defence.equals(Power.AC))
				attack = attack_vs_ac;
			else
				attack = attack_vs_other;

		return new Power(name, attack_type, damage_type, attack, area, range, defence, num_dice, die, damage_mod, action_type, power_type, recharge, description);
	}

	private Other_Power editOtherPower(Other_Power p)
	{
		/*
		 *Name
		 *action type
		 *power type
		 *recharge
		 *description
		 */
		Object chosenEdit;
		if(p.getPowerType().equals(Power.RECHARGE))
		{
			Object[] powerOptions = {"Name", "Action Type", "Power Type", "Recharge", "Description"};
			chosenEdit = powerOptions[0];
			chosenEdit = JOptionPane.showInputDialog(null, "What would you like to edit?", name, JOptionPane.QUESTION_MESSAGE, null, powerOptions, powerOptions[0]);
			if(chosenEdit == null)
				chosenEdit = "";
		}
		else
		{
			Object[] powerOptions = {"Name", "Action Type", "Power Type", "Description"};
			chosenEdit = powerOptions[0];
			chosenEdit = JOptionPane.showInputDialog(null, "What would you like to edit?", name, JOptionPane.QUESTION_MESSAGE, null, powerOptions, powerOptions[0]);
			if(chosenEdit == null)
				chosenEdit = "";
		}

		String name = p.getName();
		String action_type = p.getActionType();
		String power_type = p.getPowerType();
		String recharge = p.getRecharge();
		String description = p.getDescription();

		if(chosenEdit.equals("Name"))
		{
			String old_name = name;
			name = JOptionPane.showInputDialog(null, "Enter new name", "Renaming...", JOptionPane.QUESTION_MESSAGE);
			if(name == null)
				name = old_name;
		}
		else if(chosenEdit.equals("Action Type"))
		{
			Object[] actionTypes = {Power.STANDARD, Power.MOVE, Power.MINOR, Power.IMMEDIATE};
			action_type = (String)JOptionPane.showInputDialog(null, "Select the type of action", name, JOptionPane.QUESTION_MESSAGE, null, actionTypes, actionTypes[0]);
		}
		else if(chosenEdit.equals("Power Type"))
		{
			Object[] powerTypes = {Power.AT_WILL, Power.ENCOUNTER, Power.RECHARGE, Power.DAILY};
			power_type = (String)JOptionPane.showInputDialog(null, "Select the type of power", name, JOptionPane.QUESTION_MESSAGE, null, powerTypes, powerTypes[0]);
			if(power_type.equals("Recharge"))
			{
				Object[] recharges = {"1+", "2+", "3+", "4+", "5+", "6"};
				recharge = (String)JOptionPane.showInputDialog(null, "Select recharge threshold", name, JOptionPane.QUESTION_MESSAGE, null, recharges, recharges[0]);
			}
			else
				recharge = null;
		}
		else if(chosenEdit.equals("Recharge"))
		{
			Object[] recharges = {"1+", "2+", "3+", "4+", "5+", "6"};
			recharge = (String)JOptionPane.showInputDialog(null, "Select recharge threshold", name, JOptionPane.QUESTION_MESSAGE, null, recharges, recharges[0]);
		}
		else if(chosenEdit.equals("Description"))
		{
			String old_description = description;
			description = JOptionPane.showInputDialog(null, "Describe the power", name, JOptionPane.QUESTION_MESSAGE);
			if(description == null)
				description = old_description;
		}

		return new Other_Power(name, action_type, power_type, recharge, description);
	}

	public void load()
	{
		Object[] load_monsters = monsters.toArray();

		Object load_monster = JOptionPane.showInputDialog(null, "What monster would you like to load?", "Loading monster...", JOptionPane.QUESTION_MESSAGE, null, load_monsters, load_monsters[0]);
		if(load_monster == null)
			load_monster = name;
		load((String)load_monster);
	}

	public void load(String load_monster)
	{
//		Object[] load_monsters = monsters.toArray();
//
//		Object load_monster = JOptionPane.showInputDialog(null, "What monster would you like to load?", "Loading monster...", JOptionPane.QUESTION_MESSAGE, null, load_monsters, load_monsters[0]);
//		if(load_monster == null)
//			load_monster = name;

		String loadName = Constants.DATA_FOLDER + "\\" + Constants.MONSTER_FOLDER + "\\" + load_monster + ".txt";
		name = load_monster;

		normal_data.clear();
		trained_skills.clear();
		power_data.clear();
		other_power_data.clear();

		boolean reading_skills = false;
		boolean reading_powers = false;
		boolean reading_other_powers = false;

		BufferedReader monster_loader = getReader(loadName);

		String line = "";

		try
		{
			line = monster_loader.readLine();
		}
		catch(IOException e)
		{
			System.exit(0);
		}

		while(line != null)
		{
			if(line.equals("//skills//"))
			{
				reading_skills = true;
				try
				{
					line = monster_loader.readLine();
				}
				catch(IOException e)
				{
					System.exit(0);
				}
			}
			else if(line.equals("//attack powers//"))
			{
				reading_skills = false;
				reading_powers = true;
				try
				{
					line = monster_loader.readLine();
				}
				catch(IOException e)
				{
					System.exit(0);
				}
			}
			else if(line.equals("//other powers//"))
			{
				reading_powers = false;
				reading_other_powers = true;
				try
				{
					line = monster_loader.readLine();
				}
				catch(IOException e)
				{
					System.exit(0);
				}
			}
			if(!reading_skills && !reading_powers && !reading_other_powers)
			{
				normal_data.add(line);
				try
				{
					line = monster_loader.readLine();
				}
				catch(IOException e)
				{
					System.exit(0);
				}
			}
			else if(reading_skills)
			{
				trained_skills.add(line);
				try
				{
					line = monster_loader.readLine();
				}
				catch(IOException e)
				{
					System.exit(0);
				}
			}
			else if(reading_powers)
			{
				power_data.add(line);
				try
				{
					line = monster_loader.readLine();
				}
				catch(IOException e)
				{
					System.exit(0);
				}
			}
			else if(reading_other_powers)
			{
				other_power_data.add(line);
				try
				{
					line = monster_loader.readLine();
				}
				catch(IOException e)
				{
					System.exit(0);
				}
			}
		}
		processData();
		try
		{
			monster_loader.close();
		}
		catch(IOException e)
		{
			System.out.println("IO Error");
		}
	}

	private void processData()
	{
		String[] data = new String[normal_data.size()];
		for(int i = 0; i < normal_data.size(); i++)
			data[i] = normal_data.get(i);

		level = Integer.parseInt(data[0]);
		xp = Integer.parseInt(data[1]);
		type = data[2];
		role = data[3];
		initiative = Integer.parseInt(data[4]);
		vision = data[5];
		health = Integer.parseInt(data[6]);
		ac = Integer.parseInt(data[7]);
		fort = Integer.parseInt(data[8]);
		ref = Integer.parseInt(data[9]);
		will = Integer.parseInt(data[10]);
		speed = Integer.parseInt(data[11]);
		strength = Integer.parseInt(data[12]);
		constitution = Integer.parseInt(data[13]);
		dexterity = Integer.parseInt(data[14]);
		intelligence = Integer.parseInt(data[15]);
		wisdom = Integer.parseInt(data[16]);
		charisma = Integer.parseInt(data[17]);

		main_attribute = data[18];
		main1 = data[19];
		main2 = data[20];
		main3 = data[21];

		attack_vs_ac = Integer.parseInt(data[22]);
		attack_vs_other = Integer.parseInt(data[23]);
		attack_vs_multiple = Integer.parseInt(data[24]);

		strmod = strength/2-5+level/2;
		conmod = constitution/2-5+level/2;
		dexmod = dexterity/2-5+level/2;
		intmod = intelligence/2-5+level/2;
		wismod = wisdom/2-5+level/2;
		chamod = charisma/2-5+level/2;

		acrobatics = dexmod;
		arcana = intmod;
		athletics = strmod;
		bluff = chamod;
		diplomacy = chamod;
		dungeoneering = wismod;
		endurance = conmod;
		heal = wismod;
		history = intmod;
		insight = wismod;
		intimidate = chamod;
		nature = wismod;
		perception = wismod;
		religion = intmod;
		stealth = dexmod;
		streetwise = chamod;
		thievery = dexmod;

		for(String s : trained_skills)
		{
			if(s.equals("Acrobatics"))
				acrobatics += 5;
			else if(s.equals("Arcana"))
				arcana += 5;
			else if(s.equals("Athletics"))
				athletics += 5;
			else if(s.equals("Bluff"))
				bluff += 5;
			else if(s.equals("Diplomacy"))
				diplomacy += 5;
			else if(s.equals("Dungeoneering"))
				dungeoneering += 5;
			else if(s.equals("Endurance"))
				endurance += 5;
			else if(s.equals("Heal"))
				heal += 5;
			else if(s.equals("History"))
				history += 5;
			else if(s.equals("Insight"))
				insight += 5;
			else if(s.equals("Intimidate"))
				intimidate += 5;
			else if(s.equals("Nature"))
				nature += 5;
			else if(s.equals("Perception"))
				perception += 5;
			else if(s.equals("Religion"))
				religion += 5;
			else if(s.equals("Stealth"))
				stealth += 5;
			else if(s.equals("Streetwise"))
				streetwise += 5;
			else if(s.equals("Thievery"))
				thievery += 5;
		}

		data = new String[power_data.size()];
		for(int i = 0; i < power_data.size(); i++)
			data[i] = power_data.get(i);

		int elements_per = 14;
		int n, at, dt, atk, a, ran, vs, nd, d, dm, act, pt, r, des;
		String recharge_in;
		powers.clear();
		for(int i = 0; i < power_data.size(); i += elements_per)
		{
			n = 0+i; at = 4+i; dt = 12+i; atk = 7+i; a = 5+i; ran = 6+i; vs = 8+i; nd = 9+i;
			d = 10+i; dm = 11+i; act = 1+i; pt = 2+i; r = 3+i; des = 13+i;

			if(power_data.size()>0)
			{
				if(data[r].equals("0"))
					recharge_in = "";
				else
					recharge_in = data[r];

				powers.add(new Power(data[n], data[at], data[dt], Integer.parseInt(data[atk]), Integer.parseInt(data[a]), Integer.parseInt(data[ran]), data[vs], Integer.parseInt(data[nd]), data[d], Integer.parseInt(data[dm]), data[act], data[pt], recharge_in, data[des]));
			}
		}

		elements_per = 5;
		data = new String[other_power_data.size()];
		for(int i = 0; i < other_power_data.size(); i++)
			data[i] = other_power_data.get(i);

		other_powers.clear();

		for(int i = 0; i < other_power_data.size(); i += elements_per)
		{
			n = 0+i; at = 1+i; pt = 2+i; r = 3+i; d = 4+i;

			if(other_power_data.size()>1)
			{
				if(data[r].equals("0"))
					recharge_in = "";
				else
					recharge_in = data[r];

				other_powers.add(new Other_Power(data[n], data[at], data[pt], recharge_in, data[d]));
			}
		}
		done_creating = true;
	}

	private void saveMonsterFile()
	{
		String save_name = Constants.DATA_FOLDER + "\\" + Constants.MONSTER_FOLDER + "\\monsters.txt";
		File f1 = new File(save_name);
		f1.delete();
		PrintWriter saveMonsterWriter = openWriter(save_name);

		for(int i = 0; i < monsters.size(); i++)
			saveMonsterWriter.println(monsters.get(i));
	}

	public void save()
	{
		File old_file = new File(Constants.DATA_FOLDER + "\\" + Constants.MONSTER_FOLDER + "\\" + old_name + ".txt");

		if(name_changed)
		{
			for(int i = 0; i< monsters.size(); i++)
				if(monsters.get(i).equals(old_name))
				{
					monsters.remove(i);
					break;
				}
			name_changed = false;
		}

		for(int i = 0; i < monsters.size(); i++)
		{
			if(monsters.get(i).equals(name))
				monsters.remove(i);
		}
		monsters.add(name);
		saveMonsterFile();

		String save_name = Constants.DATA_FOLDER + "\\"  + Constants.MONSTER_FOLDER + "\\"+ name + ".txt";
		File f1 = new File(save_name);
		if(f1.delete())
			System.out.println("Overwriting previous monster save...");
		else
			System.out.println("Creating new monster save...");
		PrintWriter saveMonsterWriter = openWriter(save_name);

		saveMonsterWriter.println(level);
		saveMonsterWriter.println(xp);
		saveMonsterWriter.println(type);
		saveMonsterWriter.println(role);
		saveMonsterWriter.println(initiative);
		saveMonsterWriter.println(vision);
		saveMonsterWriter.println(health);
		saveMonsterWriter.println(ac);
		saveMonsterWriter.println(fort);
		saveMonsterWriter.println(ref);
		saveMonsterWriter.println(will);
		saveMonsterWriter.println(speed);
		saveMonsterWriter.println(strength);
		saveMonsterWriter.println(constitution);
		saveMonsterWriter.println(dexterity);
		saveMonsterWriter.println(intelligence);
		saveMonsterWriter.println(wisdom);
		saveMonsterWriter.println(charisma);

		saveMonsterWriter.println(main_attribute);
		saveMonsterWriter.println(main1);
		saveMonsterWriter.println(main2);
		saveMonsterWriter.println(main3);

		saveMonsterWriter.println(attack_vs_ac);
		saveMonsterWriter.println(attack_vs_other);
		saveMonsterWriter.println(attack_vs_multiple);

		saveMonsterWriter.println("//skills//");

		for(String s : trained_skills)
		{
			saveMonsterWriter.println(s);
		}

		saveMonsterWriter.println("//attack powers//");

		for(Power p : powers)
		{
			saveMonsterWriter.println(p.getName());
			saveMonsterWriter.println(p.getActionType());
			saveMonsterWriter.println(p.getPowerType());
			if(p.getRecharge().equals(""))
				saveMonsterWriter.println("0");
			else
				saveMonsterWriter.println(p.getRecharge());
			saveMonsterWriter.println(p.getAttackType());
			saveMonsterWriter.println(p.getArea());
			saveMonsterWriter.println(p.getRange());
			saveMonsterWriter.println(p.getAttack());
			saveMonsterWriter.println(p.getDefence());
			saveMonsterWriter.println(p.getNumberOfDice());
			saveMonsterWriter.println(p.getDie());
			saveMonsterWriter.println(p.getDamageMod());
			saveMonsterWriter.println(p.getDamageType());
			saveMonsterWriter.println(p.getDescription());
		}

		saveMonsterWriter.println("//other powers//");

		for(Other_Power p : other_powers)
		{
			saveMonsterWriter.println(p.getName());
			saveMonsterWriter.println(p.getActionType());
			saveMonsterWriter.println(p.getPowerType());
			if(p.getRecharge().equals(""))
				saveMonsterWriter.println("0");
			else
				saveMonsterWriter.println(p.getRecharge());
			saveMonsterWriter.println(p.getDescription());
		}

		saveMonsterWriter.close();
		System.out.println("Save Complete");
	}

	private BufferedReader getReader(String name)
	{
		BufferedReader in = null;
		try
		{
			File file = new File(name);
			in = new BufferedReader(new FileReader(file));
		}
		catch(FileNotFoundException e)
		{
			System.exit(0);
		}
		catch(IOException e)
		{
			System.exit(0);
		}
		return in;
	}

	private PrintWriter openWriter(String name)
	{
		try
		{
			File file = new File(name);
			return new PrintWriter(new BufferedWriter(new FileWriter(file)), true);
		}
		catch(IOException e)
		{
			System.out.println("I/O Error");
			System.exit(0);
		}
		return null;
	}

	private class Screen extends JComponent implements ActionListener
	{
		Timer t = new Timer(50, this);
		public Screen()
		{
			t.start();
			this.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e)
			{
				if(e.getButton() == MouseEvent.BUTTON1)
					if(apb1.intersects(e.getX(), e.getY()))
						createPowers();
					else if(emb1.intersects(e.getX(), e.getY()))
						editMonster();
					else if(save1.intersects(e.getX(), e.getY()))
						save();
					else if(load1.intersects(e.getX(), e.getY()))
						load();
					else if(new1.intersects(e.getX(), e.getY()))
						createMonster();
			}
			});
		}

		public void paint(Graphics g)
		{
			Graphics2D g2 = (Graphics2D)g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.drawImage(background, 0, 0, this);
			g.drawImage(apb1.getImage(), apb1.getHorizontalPosition(), apb1.getVerticalPosition(), this);
			g.drawImage(save1.getImage(), save1.getHorizontalPosition(), save1.getVerticalPosition(), this);
			g.drawImage(load1.getImage(), load1.getHorizontalPosition(), load1.getVerticalPosition(), this);
			g.drawImage(new1.getImage(), new1.getHorizontalPosition(), new1.getVerticalPosition(), this);
			if(done_creating)
			{
				g.setColor(new Color(250, 250, 250));
				g.drawImage(emb1.getImage(), emb1.getHorizontalPosition(), emb1.getVerticalPosition(), this);
				g.drawString(name, 50, 50);
				if(type.equals("Normal"))
					g.drawString("Level " + level + " " + role, 50, 60);
				else
					g.drawString("Level " + level + " " + type + " " + role, 50, 60);
				g.drawString("XP " + xp, 300, 50);
				g.drawString("Initiative +" + initiative, 50, 90);
				g.drawString("Perception +" + perception + "; " + vision, 150, 90);
				g.drawString("HP " + health + "; Bloodied " + health/2, 50, 100);
				g.drawString("AC " + ac + "; FORT " + fort + "; REF " + ref + "; WILL " + will, 50, 110);
				g.drawString("Speed " + speed, 50, 120);
				g.drawString("STR " + strength + " (+" + strmod + ")", 50, 140);
				g.drawString("CON " + constitution + " (+" + conmod + ")", 50, 150);
				g.drawString("DEX " + dexterity + " (+" + dexmod + ")", 150, 140);
				g.drawString("INT " + intelligence + " (+" + intmod + ")", 150, 150);
				g.drawString("WIS " + wisdom + " (+" + wismod + ")", 250, 140);
				g.drawString("CHA " + charisma + " (+" + chamod + ")", 250, 150);

				int skills_start = 170;

				int num_perception = 0;
				for(int i = 0; i < trained_skills.size(); i++)
				{
					if(trained_skills.get(i).equals("Perception"))
					{
						trained_skills.remove(i);
						num_perception++;
					}
				}

//				for(int i = 0; i < trainable_skills.size(); i++)
//				{
					int rowtracker = 0;
					for(int j = 0; j < trained_skills.size(); j++)
					{
						int hpos;
						if(j%2 == 1)
						{
							hpos = 200;
						}
						else
						{
							hpos = 50;
						}
//						if(trainable_skills.get(i).equals(trained_skills.get(j)))
							if(trained_skills.get(j).equals("Acrobatics"))
								g.drawString("Acrobatics +" + acrobatics, hpos, skills_start+rowtracker*10);
							else if(trained_skills.get(j).equals("Arcana"))
								g.drawString("Arcana +" + arcana, hpos, skills_start+rowtracker*10);
							else if(trained_skills.get(j).equals("Athletics"))
								g.drawString("Athletics +" + athletics, hpos, skills_start+rowtracker*10);
							else if(trained_skills.get(j).equals("Bluff"))
								g.drawString("Bluff +" + bluff, hpos, skills_start+rowtracker*10);
							else if(trained_skills.get(j).equals("Diplomacy"))
								g.drawString("Diplomacy +" + diplomacy, hpos, skills_start+rowtracker*10);
							else if(trained_skills.get(j).equals("Dungeoneering"))
								g.drawString("Dungeoneering +" + dungeoneering, hpos, skills_start+rowtracker*10);
							else if(trained_skills.get(j).equals("Endurance"))
								g.drawString("Endurance +" + endurance, hpos, skills_start+rowtracker*10);
							else if(trained_skills.get(j).equals("Heal"))
								g.drawString("Heal +" + heal, hpos, skills_start+rowtracker*10);
							else if(trained_skills.get(j).equals("History"))
								g.drawString("History +" + history, hpos, skills_start+rowtracker*10);
							else if(trained_skills.get(j).equals("Insight"))
								g.drawString("Insight +" + insight, hpos, skills_start+rowtracker*10);
							else if(trained_skills.get(j).equals("Intimidate"))
								g.drawString("Intimidate +" + intimidate, hpos, skills_start+rowtracker*10);
							else if(trained_skills.get(j).equals("Nature"))
								g.drawString("Nature +" + nature, hpos, skills_start+rowtracker*10);
							else if(trained_skills.get(j).equals("Religion"))
								g.drawString("Religion +" + religion, hpos, skills_start+rowtracker*10);
							else if(trained_skills.get(j).equals("Stealth"))
								g.drawString("Stealth +" + stealth, hpos, skills_start+rowtracker*10);
							else if(trained_skills.get(j).equals("Streetwise"))
								g.drawString("Streetwise +" + streetwise, hpos, skills_start+rowtracker*10);
							else if(trained_skills.get(j).equals("Thievery"))
								g.drawString("Thievery +" + thievery, hpos, skills_start+rowtracker*10);
						if(j%2 == 1)
						{
							rowtracker++;
						}
					}
//				}

				int power_start;
				if(trained_skills.size() > 0)
					power_start = skills_start + trained_skills.size()/2*10+10+10*(trained_skills.size()%2);
				else
					power_start = skills_start;

				for(int i = 0; i < num_perception; i++)
				{
					trained_skills.add("Perception");
				}
				num_perception = 0;


				int other_power_start = power_start+50+50*(powers.size()-1);
				for(int i = 0; i < powers.size(); i++)
				{
					/*
					 *final line of final power is printed at power_start+30+50*powers.size()-1
					 */
					if(powers.get(i).getRecharge().equals(""))
						g.drawString(powers.get(i).getName() + " (" + powers.get(i).getActionType() + "; " + powers.get(i).getPowerType() + ")", 50, power_start+50*i);
					else
						g.drawString(powers.get(i).getName() + " (" + powers.get(i).getActionType() + "; " + powers.get(i).getPowerType() + " " + powers.get(i).getRecharge() + ")", 50, power_start+50*i);
					if(powers.get(i).getAttackType().equals(Power.CLOSE_BURST) || powers.get(i).getAttackType().equals(Power.CLOSE_BLAST))
						g.drawString(powers.get(i).getAttackType() + " " + powers.get(i).getArea(), 50, power_start+10+50*i);
					else if(powers.get(i).getAttackType().equals(Power.BURST) || powers.get(i).getAttackType().equals(Power.BLAST))
						g.drawString(powers.get(i).getAttackType() + " " + powers.get(i).getArea() + " within " + powers.get(i).getRange(), 50, power_start+10+50*i);
					else
						g.drawString(powers.get(i).getAttackType() + " " + powers.get(i).getRange(), 50, power_start+10+50*i);
					g.drawString("+" + powers.get(i).getAttack() + " vs " + powers.get(i).getDefence() + "; " + powers.get(i).getNumberOfDice() + powers.get(i).getDie() + "+" + powers.get(i).getDamageMod() + " " + powers.get(i).getDamageType() + " damage", 50, power_start+20+50*i);
					g.drawString(powers.get(i).getDescription(), 50, power_start+30+50*i);
				}
				for(int i = 0; i < other_powers.size(); i++)
				{
					if(other_powers.get(i).getRecharge().equals(""))
						g.drawString(other_powers.get(i).getName() + " (" + other_powers.get(i).getActionType() + "; " + other_powers.get(i).getPowerType() + ")", 50, other_power_start+30*i);
					else
						g.drawString(other_powers.get(i).getName() + " (" + other_powers.get(i).getActionType() + "; " + other_powers.get(i).getPowerType() + " " + other_powers.get(i).getRecharge() + ")", 50, other_power_start+30*i);
					g.drawString(other_powers.get(i).getDescription(), 50, other_power_start+10+30*i);
				}
			}
		}

		public void actionPerformed(ActionEvent event)
		{
			repaint();
		}
	}

	private boolean isInteger(String s)
	{
		try
		{
			Integer.parseInt(s);
			return true;
		}
		catch(Exception e)
		{
			return false;
		}
	}
}

class Monster
{
	String name, tag = "";
	boolean displayable = false;
	int level, xp, health, initiative, initiative_score;
	ArrayList<Power> powers = new ArrayList<Power>();
	Power main_power;

	public Monster(String n, int l, int x, int h, int i, ArrayList<Power> p)
	{
		name = n;
		level = l;
		xp = x;
		health = h;
		initiative = i;
		powers = p;
	}

	public Monster(Monster m)
	{
		name = m.getName();
		level = m.getLevel();
		xp = m.getXP();
		health = m.getHealth();
		initiative = m.getInitiative();
		initiative_score = m.getInitiativeScore();
		powers = m.getPowers();
	}

	public void setInitiativeScore()
	{
		initiative_score = (int)(Math.random()*20+1)+initiative;
	}

	public void setInitiativeScore(int i)
	{
		initiative_score = i;
	}

	public void setTag(String t)
	{
		tag = t;
	}

	public void doDamage(int dmg)
	{
		health -= dmg;
	}

	public void setMainPower()
	{
		String[] powerOptions = new String[powers.size()];
		String chosen_power = "Error";
		for(int i = 0; i < powers.size(); i++)
			powerOptions[i] = powers.get(i).getName();
		if(powerOptions.length == 0)
			JOptionPane.showMessageDialog(null, "No powers available", "Error", JOptionPane.ERROR_MESSAGE);
		else
		{
			chosen_power = (String)JOptionPane.showInputDialog(null, "Select the main power", name, JOptionPane.QUESTION_MESSAGE, null, powerOptions, powerOptions[0]);
			if(chosen_power == null)
				chosen_power = powerOptions[0];
		}
		for(int i = 0; i < powers.size(); i++)
		{
			if(chosen_power.equals(powers.get(i).getName()))
				main_power = powers.get(i);
		}
	}

	public void setMainPower(Power p)
	{
		main_power = p;
	}

	public void setMainPower(String p)
	{
		for(int i = 0; i < powers.size(); i++)
		{
			if(p.equals(powers.get(i).getName()))
				main_power = powers.get(i);
		}
	}

	public void makeDisplayable()
	{
		displayable = true;
	}

	public boolean isDisplayable(){return displayable;}
	public String getName(){return name;}
	public int getLevel(){return level;}
	public int getXP(){return xp;}
	public int getHealth(){return health;}
	public int getInitiative(){return initiative;}
	public int getInitiativeScore(){return initiative_score;}
	public ArrayList<Power> getPowers(){return powers;}
	public Power getMainPower(){return main_power;}
	public String getTag(){return tag;}
}

class EncounterPlayer extends Monster
{
	public EncounterPlayer(String n, int i)
	{
		super(n, 0, 0, 0, i, new ArrayList<Power>());
		setInitiativeScore(i);
	}
	public EncounterPlayer(EncounterPlayer p)
	{
		super(p.getName(), 0, 0, 0, p.getInitiative(), new ArrayList<Power>());
		setInitiativeScore(p.getInitiativeScore());
	}
}

class EncounterMaker extends JFrame
{
	static final int ENCOUNTER_FRAME_H = 1000;
	static final int ENCOUNTER_FRAME_V = 600;

	String encounter_name = "Encounter 1";

	int v_spacing = 40;
	int startx = 50, starty = 50;
	int outline_offset = 14;

	int edit_startx;
	int edit_starty = starty-15;
	int remove_startx;
	int remove_starty = starty-15;
	int view_startx;
	int view_starty = starty-15;

	int longest_name = 0;

	int total_xp = 0;

	int s = 0;

	Toolkit kit = Toolkit.getDefaultToolkit();
	Image background = kit.getImage(Constants.IMAGE_FOLDER + "\\encounterbackground.png");
	ArrayList<String> monster_names = new ArrayList<String>();
	int monster_index = 0;

	ArrayList<Monster> available_monsters = new ArrayList<Monster>();
	ArrayList<String> monster_data = new ArrayList<String>();
	ArrayList<String> power_data = new ArrayList<String>();

	ArrayList<Monster> monsters = new ArrayList<Monster>();
	ArrayList<String> encounters = new ArrayList<String>();

	ArrayList<EditButton> edit_buttons = new ArrayList<EditButton>();
	ArrayList<RemoveButton> remove_buttons = new ArrayList<RemoveButton>();
	ArrayList<ViewButton> view_buttons = new ArrayList<ViewButton>();

	int buttonx = ENCOUNTER_FRAME_H-100;
	int sbuttony = ENCOUNTER_FRAME_V-70;
	int lbuttony = sbuttony-30;
	int rbuttony = lbuttony-30;
	int abuttony = rbuttony-30;

	AddButton ab = new AddButton(buttonx, abuttony);
	SaveButton sb = new SaveButton(buttonx, sbuttony);
	LoadButton lb = new LoadButton(buttonx, lbuttony);
	RunButton rb = new RunButton(buttonx, rbuttony);

	boolean running = false;
	ArrayList<AttackButton> attack_buttons = new ArrayList<AttackButton>();
	ArrayList<TakeDamageButton> take_damage_buttons = new ArrayList<TakeDamageButton>();
	ArrayList<ViewButton> run_view_buttons = new ArrayList<ViewButton>();
	ArrayList<TagButton> tag_buttons = new ArrayList<TagButton>();
	ArrayList<Monster> combatants = new ArrayList<Monster>();

	public EncounterMaker()
	{
		this.setSize(ENCOUNTER_FRAME_H, ENCOUNTER_FRAME_V);
    	this.setTitle("EncounterMaker");
    	this.add(new Screen(), BorderLayout.CENTER);
    	this.setVisible(true);
    	this.setLocationRelativeTo(null);
    	this.setResizable(true);
    	getMonsters();
    	getEncounters();
//    	chooseMonsters();
//    	getPlayers();
	}

	private void chooseMonsters()
	{
		int index_of_monsters = 0;
		String[] monsterOptions = new String[monster_names.size()+1];
		monsterOptions[0] = "Done";
		for(int i = 0; i < monster_names.size(); i++)
		{
			monsterOptions[i+1] = monster_names.get(i);
		}
		String chosenMonster = "";
		while(chosenMonster != null && !chosenMonster.equals("Done"))
		{
			chosenMonster = (String)JOptionPane.showInputDialog(null, "Select a monster", "Monsters", JOptionPane.QUESTION_MESSAGE, null, monsterOptions, monsterOptions[0]);
			if(chosenMonster != null && !chosenMonster.equals("Done"))
			{
				int i;
				for(i = 0; i < monster_names.size(); i++)
				{
					if(monster_names.get(i).equals(chosenMonster))
						break;
				}
				String in = "";
				while(!isInteger(in))
					in = JOptionPane.showInputDialog(null, "How many?", monster_names.get(i), JOptionPane.QUESTION_MESSAGE);
				int num = Integer.parseInt(in);

				for(int j = 0; j < num; j++)
				{
					monsters.add(new Monster(available_monsters.get(i)));
					if(j == 0)
						monsters.get(index_of_monsters).setMainPower();
					else
						monsters.get(j).setMainPower(monsters.get(index_of_monsters-j).getMainPower());
					edit_buttons.add(new EditButton(edit_startx, edit_starty+index_of_monsters*v_spacing));
					remove_buttons.add(new RemoveButton(remove_startx, remove_starty+index_of_monsters*v_spacing));
					view_buttons.add(new ViewButton(view_startx, view_starty+index_of_monsters*v_spacing));
					index_of_monsters++;
				}
				for(int j = index_of_monsters-num; j < index_of_monsters; j++)
					monsters.get(j).makeDisplayable();
			}
		}
		sortMonsters();
		calculate();
	}

	private void run()
	{
		combatants.clear();
		for(Monster m : monsters)
		{
			combatants.add(m);
			m.setInitiativeScore();
		}
		String name = JOptionPane.showInputDialog(null, "Enter player name, or 'Done' to finish", "Players", JOptionPane.QUESTION_MESSAGE);
		int initiative;
		while(!name.equalsIgnoreCase("done") && name != null)
		{
			String in = "";
			while(!isInteger(in))
				in = JOptionPane.showInputDialog(null, "Enter initiative score", name, JOptionPane.QUESTION_MESSAGE);
			initiative = Integer.parseInt(in);
			combatants.add(new EncounterPlayer(name, initiative));
			name = JOptionPane.showInputDialog(null, "Enter player name, or 'Done' to finish", "Players", JOptionPane.QUESTION_MESSAGE);
		}
		sortCombatants();
		fillButtonLists();
		running = true;
	}

	private void fillButtonLists()
	{
		for(int i = 0; i < combatants.size(); i++)
			if(combatants.get(i) instanceof EncounterPlayer)
			{
				attack_buttons.add(null);
				take_damage_buttons.add(null);
				run_view_buttons.add(null);
				tag_buttons.add(null);
			}
			else
			{
				attack_buttons.add(new AttackButton(0, edit_startx+100, edit_starty+i*v_spacing));
				take_damage_buttons.add(new TakeDamageButton(0, edit_startx+90+100, edit_starty+i*v_spacing));
				run_view_buttons.add(new ViewButton(edit_startx+180+100, edit_starty+i*v_spacing));
				tag_buttons.add(new TagButton(edit_startx+230+100, edit_starty+i*v_spacing));
			}
	}

	private void sortCombatants()
	{
		for(int i = 0; i < combatants.size()-1; i++)
		{
			for(int j = i+1; j < combatants.size(); j++)
			{
				if(combatants.get(i).getInitiativeScore() < combatants.get(j).getInitiativeScore())
				{
					if(combatants.get(i) instanceof EncounterPlayer && combatants.get(j) instanceof EncounterPlayer)
					{
						EncounterPlayer tmp = (EncounterPlayer)combatants.get(i);
						combatants.set(i, new EncounterPlayer((EncounterPlayer)combatants.get(j)));
						combatants.set(j, new EncounterPlayer(tmp));
						combatants.get(i).makeDisplayable();
						combatants.get(j).makeDisplayable();
					}
					else if(combatants.get(i) instanceof Monster && combatants.get(j) instanceof EncounterPlayer)
					{
						Monster tmp = combatants.get(i);
						combatants.set(i, new EncounterPlayer((EncounterPlayer)combatants.get(j)));
						combatants.set(j, new Monster(tmp));
						combatants.get(j).setMainPower(tmp.getMainPower());
						combatants.get(i).makeDisplayable();
						combatants.get(j).makeDisplayable();
					}
					else if(combatants.get(i) instanceof EncounterPlayer && combatants.get(j) instanceof Monster)
					{
						Monster tmp = combatants.get(j);
						combatants.set(j, new EncounterPlayer((EncounterPlayer)combatants.get(i)));
						combatants.set(i, new Monster(tmp));
						combatants.get(i).setMainPower(tmp.getMainPower());
						combatants.get(i).makeDisplayable();
						combatants.get(j).makeDisplayable();
					}
					else
					{
						Monster tmp = combatants.get(i);
						combatants.set(i, new Monster(combatants.get(j)));
						combatants.get(i).setMainPower(combatants.get(j).getMainPower());
						combatants.set(j, new Monster(tmp));
						combatants.get(j).setMainPower(tmp.getMainPower());
						combatants.get(i).makeDisplayable();
						combatants.get(j).makeDisplayable();
					}
				}
			}
		}
	}

	private void addMonsters()
	{
		String[] monsterOptions = new String[monster_names.size()];
		for(int i = 0; i < monster_names.size(); i++)
		{
			monsterOptions[i] = monster_names.get(i);
		}
		String chosenMonster = (String)JOptionPane.showInputDialog(null, "Select a monster", "Monsters", JOptionPane.QUESTION_MESSAGE, null, monsterOptions, monsterOptions[0]);
		if(chosenMonster != null)
		{
			int i;
			for(i = 0; i < monster_names.size(); i++)
			{
				if(monster_names.get(i).equals(chosenMonster))
					break;
			}
			String in = "";
			while(!isInteger(in))
				in = JOptionPane.showInputDialog(null, "How many?", monster_names.get(i), JOptionPane.QUESTION_MESSAGE);
			int num = Integer.parseInt(in);

			for(int j = 0; j < num; j++)
			{
				monsters.add(new Monster(available_monsters.get(i)));
				boolean already_added = false;
				for(int k = 0; k < monsters.size()-1; k++)
					if(monsters.get(monsters.size()-1).getName().equals(monsters.get(k).getName()))
					{
						monsters.get(monsters.size()-1).setMainPower(monsters.get(k).getMainPower());
						already_added = true;
						break;
					}
				if(!already_added)
					monsters.get(monsters.size()-1).setMainPower();
				edit_buttons.add(new EditButton(edit_startx, edit_starty+(edit_buttons.size())*v_spacing));
				remove_buttons.add(new RemoveButton(remove_startx, remove_starty+(remove_buttons.size())*v_spacing));
				view_buttons.add(new ViewButton(view_startx, view_starty+(view_buttons.size())*v_spacing));
			}
			for(int j = monsters.size()-num; j < monsters.size(); j++)
					monsters.get(j).makeDisplayable();
		}
		sortMonsters();
		calculate();
	}

	private void sortMonsters()
	{
		for(int i = 0; i < monsters.size()-1; i++)
		{
			for(int j = i+1; j < monsters.size(); j++)
			{
				if(monsters.get(i).getName().compareTo(monsters.get(j).getName()) > 0)
				{
					Monster tmp = monsters.get(i);
					monsters.set(i, new Monster(monsters.get(j)));
					monsters.get(i).setMainPower(monsters.get(j).getMainPower());
					monsters.set(j, new Monster(tmp));
					monsters.get(j).setMainPower(tmp.getMainPower());
					monsters.get(i).makeDisplayable();
					monsters.get(j).makeDisplayable();
				}
			}
		}
	}

	private void save()
	{
		String newName = (String)JOptionPane.showInputDialog(null, "Name the encounter", encounter_name, JOptionPane.QUESTION_MESSAGE);
		String save_name = Constants.DATA_FOLDER + "\\"  + Constants.ENCOUNTER_FOLDER + "\\" + newName + ".txt";
		File f1 = new File(save_name);
		if(f1.delete())
			System.out.println("Overwriting previous encounter save...");
		else
			System.out.println("Creating new encounter save...");

		File old_file = new File(Constants.DATA_FOLDER + "\\"  + Constants.ENCOUNTER_FOLDER + "\\" + encounter_name + ".txt");
		old_file.delete();

		for(int i = 0; i < encounters.size(); i++)
			if(encounters.get(i).equals(encounter_name))
			{
				encounters.remove(i);
				break;
			}

		encounters.add(newName);

		String encounter_file_name = Constants.DATA_FOLDER + "\\" + Constants.ENCOUNTER_FOLDER + "\\encounters.txt";
		File f2 = new File(encounter_file_name);
		f2.delete();
		PrintWriter saveEncounterFileWriter = openWriter(encounter_file_name);

		for(int i = 0; i < encounters.size(); i++)
			saveEncounterFileWriter.println(encounters.get(i));

		encounter_name = newName;
		this.setTitle(newName);

		PrintWriter saveEncounterWriter = openWriter(save_name);

		for(Monster m: monsters)
		{
			saveEncounterWriter.println(m.getName());
			saveEncounterWriter.println(m.getMainPower().getName());
		}

		saveEncounterWriter.close();
		saveEncounterFileWriter.close();
		System.out.println("Save complete");
	}

	private void load()
	{
		if(encounters.size() > 0)
		{
			Object[] load_encounters = encounters.toArray();

			Object load_encounter = JOptionPane.showInputDialog(null, "What encounter would you like to load?", "Loading encounter...", JOptionPane.QUESTION_MESSAGE, null, load_encounters, load_encounters[0]);
			if(load_encounter == null)
				load_encounter = encounter_name;
			load((String)load_encounter);
		}
		else
		{
			JOptionPane.showMessageDialog(null, "No encounters available", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void load(String enc)
	{
		encounter_name = enc;
		this.setTitle(encounter_name);
		monsters.clear();
		edit_buttons.clear();
		remove_buttons.clear();
		view_buttons.clear();

		String load_name = Constants.DATA_FOLDER + "\\" + Constants.ENCOUNTER_FOLDER + "\\" + encounter_name + ".txt";
		File f1 = new File(load_name);

		BufferedReader encounter_loader = getReader(load_name);

		String line = "";
		int line_tracker = 0;

		try
		{
			line = encounter_loader.readLine();
			line_tracker++;
		}
		catch(IOException e)
		{
			System.exit(0);
		}

		while(line != null)
		{
			if((line_tracker-1)%2 == 0)
			{
				int i;
				for(i = 0; i < monster_names.size(); i++)
				{
					if(monster_names.get(i).equals(line))
						break;
				}

				monsters.add(new Monster(available_monsters.get(i)));
				edit_buttons.add(new EditButton(edit_startx, edit_starty+(edit_buttons.size())*v_spacing));
				remove_buttons.add(new RemoveButton(remove_startx, remove_starty+(remove_buttons.size())*v_spacing));
				view_buttons.add(new ViewButton(view_startx, view_starty+(view_buttons.size())*v_spacing));

				monsters.get(monsters.size()-1).makeDisplayable();
			}
			else
			{
				monsters.get(monsters.size()-1).setMainPower(line);
			}
			try
			{
				line = encounter_loader.readLine();
				line_tracker++;
			}
			catch(IOException e)
			{
				System.exit(0);
			}
		}
		try{
			encounter_loader.close();
		}
		catch(IOException e)
		{
		}
		calculate();
	}

//	private void getPlayers()
//	{
//		String name = "";
//		int initiative = 0;
//		name = (String)JOptionPane.showInputDialog(null, "Enter player name or leave blank to terminate", "Players", JOptionPane.QUESTION_MESSAGE);
//		if(name != null && !name.equals(""))
//		{
//			String in = "";
//			while(!isInteger(in))
//				in = JOptionPane.showInputDialog(null, "Enter initiative score", name, JOptionPane.QUESTION_MESSAGE);
//			initiative = Integer.parseInt(in);
//		}
//		while(name != null && !name.equals(""))
//		{
//			players.add(new Player(name, initiative));
//			name = (String)JOptionPane.showInputDialog(null, "Enter player name or leave blank to terminate", "Players", JOptionPane.QUESTION_MESSAGE);
//
//			if(name != null && !name.equals(""))
//			{
//				String in = "";
//				while(!isInteger(in))
//					in = JOptionPane.showInputDialog(null, "Enter initiative score", name, JOptionPane.QUESTION_MESSAGE);
//				initiative = Integer.parseInt(in);
//			}
//		}
//	}

	private void getMonsters()
	{
		getMonsterNames();
		getAllMonsters();
	}

	private void getEncounters()
	{
		String loadName = Constants.DATA_FOLDER + "\\" + Constants.ENCOUNTER_FOLDER + "\\" + "encounters.txt";

		BufferedReader encounter_loader = getReader(loadName);

		String line = "";

		try
		{
			line = encounter_loader.readLine();
		}
		catch(IOException e)
		{
			System.exit(0);
		}

		while(line != null)
		{
			encounters.add(line);
			try
			{
				line = encounter_loader.readLine();
			}
			catch(IOException e)
			{
				System.exit(0);
			}
		}

		sortEncounters();

		try{
			encounter_loader.close();
		}
		catch(IOException e)
		{}
	}

	private void calculate()
	{
		total_xp = 0;
		for(Monster m : monsters)
			total_xp += m.getXP();
		this.setTitle(encounter_name + ": " + total_xp + "XP");
	}

	private boolean isInteger(String s)
	{
		try
		{
			Integer.parseInt(s);
			return true;
		}
		catch(Exception e)
		{
			return false;
		}
	}

	private BufferedReader getReader(String name)
	{
		BufferedReader in = null;
		try
		{
			File file = new File(name);
			in = new BufferedReader(new FileReader(file));
		}
		catch(FileNotFoundException e)
		{
			System.exit(0);
		}
		catch(IOException e)
		{
			System.exit(0);
		}
		return in;
	}

	private PrintWriter openWriter(String name)
	{
		try
		{
			File file = new File(name);
			return new PrintWriter(new BufferedWriter(new FileWriter(file)), true);
		}
		catch(IOException e)
		{
			System.out.println("I/O Error");
			System.exit(0);
		}
		return null;
	}

	private void getMonsterNames()
	{
		String loadName = Constants.DATA_FOLDER + "\\" + Constants.MONSTER_FOLDER + "\\" + "monsters.txt";

		BufferedReader monster_loader = getReader(loadName);

		String line = "";

		try
		{
			line = monster_loader.readLine();
		}
		catch(IOException e)
		{
			System.exit(0);
		}

		while(line != null)
		{
			monster_names.add(line);
			try
			{
				line = monster_loader.readLine();
			}
			catch(IOException e)
			{
				System.exit(0);
			}
		}
		sortMonsterNames();
	}

	private void getAllMonsters()
	{
		for(String monster : monster_names)
		{
			BufferedReader monster_loader = getReader(Constants.DATA_FOLDER + "\\" + Constants.MONSTER_FOLDER + "\\" + monster + ".txt");
			monster_data.clear();
			power_data.clear();
			String line = "";
			boolean reading_data = true;
			boolean reading_powers = false;
			boolean done = false;

			try{
				line = monster_loader.readLine();
			}
			catch(IOException e)
			{
				System.exit(0);
			}

			while(line != null && !done)
			{
				if(reading_data)
				{
					if(line.equals("//skills//"))
					{
						reading_data = false;
					}
					else
					{
						monster_data.add(line);
					}
					try{
						line = monster_loader.readLine();
					}
					catch(IOException e)
					{
						System.exit(0);
					}
				}
				else if(!reading_powers)
				{
					if(line.equals("//attack powers//"))
					{
						reading_powers = true;
					}
					try{
						line = monster_loader.readLine();
					}
					catch(IOException e)
					{
						System.exit(0);
					}

				}
				else if(reading_powers)
				{
					if(line.equals("//other powers//"))
					{
						done = true;
					}
					else
					{
						power_data.add(line);
					}
					try{
						line = monster_loader.readLine();
					}
					catch(IOException e)
					{
						System.exit(0);
					}
				}
			}
			processData();
			monster_index++;
		}
	}

	private void processData()
	{
		ArrayList<Power> powers = new ArrayList<Power>();
		int elements_per_power = 14;
		for(int i = 0; i < power_data.size(); i += elements_per_power)
		{
			int n = 0+i, at = 4+i, dt = 12+i, atk = 7+i, a = 5+i, ran = 6+i, vs = 8+i, nd = 9+i,
			d = 10+i, dm = 11+i, act = 1+i, pt = 2+i, r = 3+i, des = 13+i;
			powers.add(new Power(power_data.get(n), power_data.get(at), power_data.get(dt), Integer.parseInt(power_data.get(atk)),
									Integer.parseInt(power_data.get(a)), Integer.parseInt(power_data.get(ran)), power_data.get(vs),
									Integer.parseInt(power_data.get(nd)), power_data.get(d), Integer.parseInt(power_data.get(dm)),
									power_data.get(act), power_data.get(pt), power_data.get(r), power_data.get(des)));
		}
		available_monsters.add(new Monster(monster_names.get(monster_index), Integer.parseInt(monster_data.get(0)), Integer.parseInt(monster_data.get(1)), Integer.parseInt(monster_data.get(6)), Integer.parseInt(monster_data.get(4)), powers));
	}

	private void sortEncounters()
	{
		String[] tmpenc = new String[encounters.size()];
		for(int i = 0; i < encounters.size(); i++)
			tmpenc[i] = encounters.get(i);
		Arrays.sort(tmpenc);

		encounters.clear();
		for(String s : tmpenc)
			encounters.add(s);
	}

	private void sortMonsterNames()
	{
		String[] tmpmonsters = new String[monster_names.size()];
		for(int i = 0; i < monster_names.size(); i++)
			tmpmonsters[i] = monster_names.get(i);
		Arrays.sort(tmpmonsters);

		monster_names.clear();
		for(String s : tmpmonsters)
			monster_names.add(s);
	}

	private void editMonster(int index)
	{
		String[] monsterOptions = new String[monster_names.size()];
		for(int i = 0; i < monster_names.size(); i++)
		{
			monsterOptions[i] = monster_names.get(i);
		}
		String chosenMonster = "";
		chosenMonster = (String)JOptionPane.showInputDialog(null, "Select a monster", "Monsters", JOptionPane.QUESTION_MESSAGE, null, monsterOptions, monsterOptions[0]);
		if(chosenMonster != null)
		{
			int i;
			for(i = 0; i < monster_names.size(); i++)
			{
				if(monster_names.get(i).equals(chosenMonster))
					break;
			}

			monsters.set(index, new Monster(available_monsters.get(i)));
			monsters.get(index).makeDisplayable();
			monsters.get(index).setMainPower();
		}
		recalculateLongestName();
		calculate();
	}

	private void removeMonster(int index)
	{
		monsters.remove(index);
		edit_buttons.remove(edit_buttons.size()-1);
		remove_buttons.remove(remove_buttons.size()-1);
		view_buttons.remove(view_buttons.size()-1);
		recalculateLongestName();
		calculate();
	}

	private void recalculateLongestName()
	{
		longest_name = 0;
		for(int i = 0; i < monsters.size(); i++)
			if(monsters.get(i).getName().length() > longest_name)
			{
				longest_name = monsters.get(i).getName().length();
			}
	}

	private void tag(int index)
	{
		combatants.get(index).setTag(JOptionPane.showInputDialog(null, "Tag the monster", combatants.get(index).getName(), JOptionPane.INFORMATION_MESSAGE));
	}

	private void doDamage(int index)
	{
		String in = "";
		while(!isInteger(in))
			in = JOptionPane.showInputDialog(null, "How much damage?", "", JOptionPane.QUESTION_MESSAGE);
		int dmg = Integer.parseInt(in);
		combatants.get(index).doDamage(dmg);
	}

	private void getDamage(int index)
	{
//		int numbelow = 0;
//		int numabove = 0;
//		for(int i = 0; i < 1000; i++)
//		{
//			if((int)(Math.random()*20+1) <= 10)
//				numbelow++;
//			else
//				numabove++;
//		}
//
//		System.out.println("<= 10: " + numbelow + "\n>=11: " + numabove);

		String name = combatants.get(index).getMainPower().getName();
		int atk_bonus = combatants.get(index).getMainPower().getAttack();
		String def = combatants.get(index).getMainPower().getDefence();
		int num_dice =  combatants.get(index).getMainPower().getNumberOfDice();
		String die_type  = combatants.get(index).getMainPower().getDie();
		int mod  = combatants.get(index).getMainPower().getDamageMod();
		String dmg_type = combatants.get(index).getMainPower().getDamageType();

		int die = Integer.parseInt(die_type.substring(1, die_type.length()));

		int attack = (int)(Math.random()*20+1)+atk_bonus;
		int damage = 0;
		for(int i = 0; i < num_dice; i++)
		{
			damage += (int)(Math.random()*die+1);
		}
		damage += mod;

		String message = attack + " against " + def
									+ "\n" + damage + " " + dmg_type + " damage";
		JOptionPane.showMessageDialog(null, message, name, JOptionPane.INFORMATION_MESSAGE);
	}

/*	private class MonsterSelectionScreen extends JFrame
	{
		final int XDIM = 500, YDIM = 100;
		Image monsterselectionbackground = kit.getImage(Constants.DATA_FOLDER + "\\" + Constants.IMAGE_FOLDER + "\\choosemonsterbackground.png");

		ArrayList<Box> boxes = new ArrayList<Box>();

		ArrayList<JComboBox> enemies = new ArrayList<JComboBox>();
		ArrayList<JTextField> numbers = new ArrayList<JTextField>();

		JButton addButton = new JButton("New Monster");
		JButton doneButton = new JButton("Done");

		ButtonListener b1 = new ButtonListener();

		JTextField enemy1 = new JTextField(2);

		JPanel monster_panel = new JPanel(new GridLayout(0, 1));
		JPanel info_panel = new JPanel(new BorderLayout());
		JPanel button_panel = new JPanel(new GridLayout(1, 0));

		public MonsterSelectionScreen()
		{
			this.setSize(XDIM, YDIM);
	    	this.setTitle("Monster Selection");
	    	this.setLayout(new GridLayout(0, 1));
	    	this.setVisible(true);
	    	this.setLocationRelativeTo(null);
	    	this.setResizable(false);

	    	addButton.addActionListener(b1);
	    	doneButton.addActionListener(b1);

	    	button_panel.add(addButton);
	    	this.add(info_panel);
	    	this.add(monster_panel);
	    	this.add(button_panel);
		}

		private void addMonster()
		{
			enemies.add(new JComboBox(monster_names.toArray()));
			numbers.add(new JTextField(2));
			update();
		}

		private void update()
		{
			for(int i = 0; i < enemies.size(); i++)
			{
				boxes.add(Box.createHorizontalBox());
				boxes.get(i).add(numbers.get(i));
				boxes.get(i).add(enemies.get(i));
				monster_panel.add(boxes.get(i));
			}
			validate();
		}

		private class ButtonListener implements ActionListener
		{
			public void actionPerformed(ActionEvent e)
			{
				if(e.getSource() == addButton)
				{
					addMonster();
				}
			}
		}
	}
*/

	private class TagWindow
	{
		int xpos, ypos;
		String text;
		public TagWindow(String t, int x, int y)
		{
			text = t;
			xpos = x;
			ypos = y;
		}
		public String getText(){return text;}
		public int getXPos(){return xpos;}
		public int getYPos(){return ypos;}
	}

	private class Screen extends JComponent implements ActionListener
	{
		Timer t = new Timer(50, this);
		TagWindow tag = null;
		public Screen()
		{
			t.start();
			this.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e)
			{
				if(e.getButton() == MouseEvent.BUTTON1)
				{
					if(!running)
					{
						for(int i = 0; i < remove_buttons.size(); i++)
							if(remove_buttons.get(i).intersects(e.getX(), e.getY()))
							{
								removeMonster(i);
								break;
							}
							else if(edit_buttons.get(i).intersects(e.getX(), e.getY()))
							{
								editMonster(i);
								break;
							}
							else if(view_buttons.get(i).intersects(e.getX(), e.getY()))
							{
								new MonsterMaker(monsters.get(i).getName());
								break;
							}
						if(ab.intersects(e.getX(), e.getY()))
							addMonsters();
						else if(sb.intersects(e.getX(), e.getY()))
							save();
						else if(lb.intersects(e.getX(), e.getY()))
							load();
						else if(rb.intersects(e.getX(), e.getY()))
							run();
					}
					else
					{
						for(int i = 0; i < attack_buttons.size(); i++)
							if(attack_buttons.get(i) != null)
							{
								if(attack_buttons.get(i).intersects(e.getX(), e.getY()))
								{
									getDamage(i);
								}
								else if(take_damage_buttons.get(i).intersects(e.getX(), e.getY()))
								{
									doDamage(i);
								}
								else if(run_view_buttons.get(i).intersects(e.getX(), e.getY()))
								{
									new MonsterMaker(combatants.get(i).getName());
								}
								else if(tag_buttons.get(i).intersects(e.getX(), e.getY()))
								{
									tag(i);
								}
							}
					}
				}
			}
			});
			this.addMouseMotionListener(new MouseMotionListener(){
				public void mouseMoved(MouseEvent e)
				{
					boolean mousedOver = false;
					for(int i = 0; i < attack_buttons.size(); i++)
					{
						if(tag_buttons.get(i) != null)
							if(tag_buttons.get(i).intersects(e.getX(), e.getY()))
							{
								tag = new TagWindow(combatants.get(i).getTag(), e.getX(), e.getY());
								mousedOver = true;
							}
					}
					if(!mousedOver)
						tag = null;
				}
				public void mouseDragged(MouseEvent e)
				{
					boolean mousedOver = false;
					for(int i = 0; i < attack_buttons.size(); i++)
					{
						if(tag_buttons.get(i) != null)
							if(tag_buttons.get(i).intersects(e.getX(), e.getY()))
							{
								tag = new TagWindow(combatants.get(i).getTag(), e.getX(), e.getY());
								mousedOver = true;
							}
					}
					if(!mousedOver)
						tag = null;
				}
			});
		}

		public void paint(Graphics g)
		{
			Graphics2D g2 = (Graphics2D)g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.drawImage(background, 0, 0, this);
			if(!running)
			{
				boolean longest_removed = true;

				for(int i = 0; i < monsters.size(); i++)
				{
					if(monsters.get(i).isDisplayable())
					{
						if(monsters.get(i).getName().length() >= longest_name)
						{
							int multiple;
							longest_name = monsters.get(i).getName().length();
							if(longest_name < 15)
								multiple = 10;
							else
								multiple = 7;
							edit_startx = longest_name*multiple+100;
							remove_startx = edit_startx+50;
							view_startx = remove_startx+90;
							for(int j = 0; j < edit_buttons.size(); j++)
							{
								edit_buttons.get(j).setHPos(edit_startx);
								remove_buttons.get(j).setHPos(remove_startx);
								view_buttons.get(j).setHPos(view_startx);
							}
							longest_removed = false;
						}
						if(longest_removed)
						{
							recalculateLongestName();
						}
						int rect_h;
						if(monsters.get(i).getName().length() < 15)
							rect_h = monsters.get(i).getName().length()*10;
						else
							rect_h = monsters.get(i).getName().length()*7;
						g.setColor(Color.BLACK);
						g.fillRect(startx-15, starty+i*v_spacing-outline_offset-5, rect_h+10, 30);
						g.setColor(Color.WHITE);
						g.fillRect(startx-10, starty+i*v_spacing-outline_offset, rect_h, 20);
						g.setColor(Color.BLACK);
						g.drawString(monsters.get(i).getName(), startx, starty+i*v_spacing);

						g.drawImage(edit_buttons.get(i).getImage(), edit_buttons.get(i).getHorizontalPosition(), edit_buttons.get(i).getVerticalPosition(), this);
						g.drawImage(remove_buttons.get(i).getImage(), remove_buttons.get(i).getHorizontalPosition(), remove_buttons.get(i).getVerticalPosition(), this);
						g.drawImage(view_buttons.get(i).getImage(), view_buttons.get(i).getHorizontalPosition(), view_buttons.get(i).getVerticalPosition(), this);
					}
				}
				g.drawImage(sb.getImage(), sb.getHorizontalPosition(), sb.getVerticalPosition(), this);
				g.drawImage(ab.getImage(), ab.getHorizontalPosition(), ab.getVerticalPosition(), this);
				g.drawImage(lb.getImage(), lb.getHorizontalPosition(), lb.getVerticalPosition(), this);
				g.drawImage(rb.getImage(), rb.getHorizontalPosition(), rb.getVerticalPosition(), this);
			}
			else
			{
				int runstartx = starty + 100;
				for(int i = 0; i < combatants.size(); i++)
				{
					int rect_h;
					if(combatants.get(i).getName().length() < 15)
						rect_h = combatants.get(i).getName().length()*10;
					else
						rect_h = combatants.get(i).getName().length()*7;
					g.setColor(Color.BLACK);
					g.fillRect(runstartx-15, starty+i*v_spacing-outline_offset-5, rect_h+10, 30);
					g.setColor(Color.WHITE);
					g.fillRect(runstartx-10, starty+i*v_spacing-outline_offset, rect_h, 20);
					g.setColor(Color.BLACK);
					g.drawString(combatants.get(i).getName(), runstartx, starty+i*v_spacing);

					if(attack_buttons.get(i) != null)
					{
						g.fillRect(startx-15, starty+i*v_spacing-outline_offset-5, 80, 30);
						g.setColor(Color.WHITE);
						g.fillRect(startx-10, starty+i*v_spacing-outline_offset, 70, 20);
						g.setColor(Color.BLACK);
						String message;
						if(combatants.get(i).getHealth() > 0)
							message = "HP: " + combatants.get(i).getHealth();
						else
							message = "Dead";
						g.drawString(message, startx, starty+i*v_spacing);

						g.drawImage(attack_buttons.get(i).getImage(), attack_buttons.get(i).getHorizontalPosition(), attack_buttons.get(i).getVerticalPosition(), this);
						g.drawImage(take_damage_buttons.get(i).getImage(), take_damage_buttons.get(i).getHorizontalPosition(), take_damage_buttons.get(i).getVerticalPosition(), this);
						g.drawImage(run_view_buttons.get(i).getImage(), run_view_buttons.get(i).getHorizontalPosition(), run_view_buttons.get(i).getVerticalPosition(), this);
						g.drawImage(tag_buttons.get(i).getImage(), tag_buttons.get(i).getHorizontalPosition(), tag_buttons.get(i).getVerticalPosition(), this);
					}
				}
				g.setColor(Color.WHITE);
				if(tag != null)
				{
					int taglength;
					int multiple;
					if(tag.getText().length() < 15)
						multiple = 10;
					else
						multiple = 9;
					g.fillRect(tag.getXPos()+15, tag.getYPos(), tag.getText().length()*multiple, 20);
					g.setColor(Color.BLACK);
					g.drawString(tag.getText(), tag.getXPos()+15+5, tag.getYPos()+15);
				}
			}
		}

		public void actionPerformed(ActionEvent event)
		{
			repaint();
		}
	}
}

class EncounterFrame extends JFrame
{
	static final int ENCOUNTER_FRAME_HORIZONTAL_SIZE = 500;
	static final int ENCOUNTER_FRAME_VERTICAL_SIZE = 500;

	Toolkit kit = Toolkit.getDefaultToolkit();
	Image background = kit.getImage(Constants.IMAGE_FOLDER + "\\encounterscreen.png");

	Image combatant_frame = kit.getImage(Constants.IMAGE_FOLDER + "\\combatantframe.png");

	Font font = new Font("Helvetica", Font.BOLD, 12);

	final int HDISTANCE_BETWEEN = 90;
	final int VDISTANCE_BETWEEN = 40;
	final int BUTTON_HPOS = 200;
	final int OFFSET = -10;
	final int DISPLAY_HPOS = 50;
	final int DISPLAY_VPOS = 50;
	final int TEXT_VSPACING = 10;
	int num_players, num_enemies;
	AttackButton[] attack_buttons;
	TakeDamageButton[] take_damage_buttons;
	OtherAttackButton other_attack_button = new OtherAttackButton(DISPLAY_HPOS+BUTTON_HPOS+HDISTANCE_BETWEEN, ENCOUNTER_FRAME_VERTICAL_SIZE-70);
	boolean combatants_initialized = false;

	Enemy[] enemies;
	Player[] players;

	Combatant[] combatants;

	public EncounterFrame()
	{
		this.setSize(ENCOUNTER_FRAME_HORIZONTAL_SIZE, ENCOUNTER_FRAME_VERTICAL_SIZE);
    	this.setTitle("Encounter");
    	this.add(new Screen(), BorderLayout.CENTER);
    	this.setVisible(true);
    	this.setLocationRelativeTo(null);
    	this.setResizable(false);
    	promptForNumbers();
    	combatants_initialized = true;
	}

	private void promptForNumbers()
	{
		String in = "";
		while(!isInteger(in))
			in = JOptionPane.showInputDialog(null, "Enter number of enemies", "Enemies", JOptionPane.QUESTION_MESSAGE);
		num_enemies = Integer.parseInt(in);
		enemies = new Enemy[num_enemies];
		attack_buttons = new AttackButton[num_enemies];
		take_damage_buttons = new TakeDamageButton[num_enemies];
		for(int i = 0; i < num_enemies; i++)
		{
			String name;
			int initiative, hp, attack, number_dice, damage_die, damage_mod;

			name = JOptionPane.showInputDialog(null, "Enter name", "Enemy " + (i+1), JOptionPane.QUESTION_MESSAGE);
			if(name == null)
				name = "Unnamed";

			in = "";
			while(!isInteger(in))
				in = JOptionPane.showInputDialog(null, "Enter initiative modifier", "Enemy " + (i+1), JOptionPane.QUESTION_MESSAGE);
			initiative = Integer.parseInt(in);

			in = "";
			while(!isInteger(in))
				in = JOptionPane.showInputDialog(null, "Enter health", "Enemy " + (i+1), JOptionPane.QUESTION_MESSAGE);
			hp = Integer.parseInt(in);

			in = "";
			while(!isInteger(in))
				in = JOptionPane.showInputDialog(null, "Enter attack modifier", "Enemy " + (i+1), JOptionPane.QUESTION_MESSAGE);
			attack = Integer.parseInt(in);

			in = "";
			while(!isInteger(in))
				in = JOptionPane.showInputDialog(null, "Enter number of damage dice", "Enemy " + (i+1), JOptionPane.QUESTION_MESSAGE);
			number_dice = Integer.parseInt(in);

			in = "";
			while(!isInteger(in))
				in = JOptionPane.showInputDialog(null, "Enter damage die", "Enemy " + (i+1), JOptionPane.QUESTION_MESSAGE);
			damage_die = Integer.parseInt(in);

			in = "";
			while(!isInteger(in))
				in = JOptionPane.showInputDialog(null, "Enter damage modifier", "Enemy " + (i+1), JOptionPane.QUESTION_MESSAGE);
			damage_mod = Integer.parseInt(in);

			enemies[i] = new Enemy(name, initiative, hp, attack, number_dice, damage_die, damage_mod);
		}

		in = "";
		while(!isInteger(in))
			in = JOptionPane.showInputDialog(null, "Enter number of players", "Players", JOptionPane.QUESTION_MESSAGE);
		num_players = Integer.parseInt(in);

		players = new Player[num_players];

		for(int i = 0; i < num_players; i++)
		{
			String name;
			int initiative;

			name = JOptionPane.showInputDialog(null, "Enter name", "Player " + (i+1), JOptionPane.QUESTION_MESSAGE);
			if(name == null)
			{
				name = "Unnamed";
			}

			in = "";
			while(!isInteger(in))
				in = JOptionPane.showInputDialog(null, "Enter initiative score", "Player " + (i+1), JOptionPane.QUESTION_MESSAGE);
			initiative = Integer.parseInt(in);

			players[i] = new Player(name, initiative);
		}

		combatants = new Combatant[num_players + num_enemies];
		for(int i = 0; i < num_players; i++)
		{
			combatants[i] = players[i];
		}
		for(int i = num_players; i < combatants.length; i++)
		{
			combatants[i] = enemies[i-num_players];
		}

		//sort by initiative
		for(int i = 0; i < combatants.length-1; i++)
		{
			for(int j = i+1; j < combatants.length; j++)
			{
				if(combatants[i].getInitiative() < combatants[j].getInitiative())
				{
					Combatant tmp = combatants[i];
					combatants[i] = combatants[j];
					combatants[j] = tmp;
				}
			}
		}
		//sorted

		int button_index = 0;
		for(int i = 0; i < combatants.length; i++)
		{
			if(combatants[i] instanceof Enemy)
			{
				attack_buttons[button_index] = new AttackButton(i, DISPLAY_HPOS+BUTTON_HPOS, DISPLAY_VPOS+i*VDISTANCE_BETWEEN+OFFSET);
				take_damage_buttons[button_index] = new TakeDamageButton(i, DISPLAY_HPOS+BUTTON_HPOS+HDISTANCE_BETWEEN, DISPLAY_VPOS+i*VDISTANCE_BETWEEN+OFFSET);
				button_index++;
			}
		}
	}

	private class Screen extends JComponent implements ActionListener
	{
		Timer t = new Timer(50, this);
		public Screen()
		{
			t.start();
			this.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e)
			{
				if(e.getButton() == MouseEvent.BUTTON1)
				{
					for(int i = 0; i < take_damage_buttons.length; i++)
						if(take_damage_buttons[i].intersects(e.getX(), e.getY()))
						{
							String in = "";
							while(!isInteger(in))
								in = JOptionPane.showInputDialog(null, "How much damage?", "Damage", JOptionPane.QUESTION_MESSAGE);
							combatants[take_damage_buttons[i].getEnemyIndex()].doDamage(Integer.parseInt(in));
						}
					for(int i = 0; i < attack_buttons.length; i++)
						if(attack_buttons[i].intersects(e.getX(), e.getY()))
						{
							int roll = combatants[attack_buttons[i].getEnemyIndex()].getAttack();
							if(roll >= 0)
								JOptionPane.showMessageDialog(null, "Attack Roll: " + roll
									+ "\nDamage Roll: " + combatants[attack_buttons[i].getEnemyIndex()].getDamage(), "Attack",
									JOptionPane.ERROR_MESSAGE);
							else
								JOptionPane.showMessageDialog(null, "Attack Roll: CRIT!"
									+ "\nDamage: " + combatants[attack_buttons[i].getEnemyIndex()].getMaxDamage(),
									"CRIT!", JOptionPane.ERROR_MESSAGE);
						}
					if(other_attack_button.intersects(e.getX(), e.getY()))
					{
						int atk, num_dice, dmg_die, dmg_mod;
						String in = "";
						while(!isInteger(in))
							in = JOptionPane.showInputDialog(null, "Enter attack modifier", "Attack", JOptionPane.QUESTION_MESSAGE);
						atk = Integer.parseInt(in);

						in = "";
						while(!isInteger(in))
							in = JOptionPane.showInputDialog(null, "Enter damage die", "Attack", JOptionPane.QUESTION_MESSAGE);
						dmg_die = Integer.parseInt(in);

						in = "";
						while(!isInteger(in))
							in = JOptionPane.showInputDialog(null, "Enter number of damage dice", "Attack", JOptionPane.QUESTION_MESSAGE);
						num_dice = Integer.parseInt(in);

						in = "";
						while(!isInteger(in))
							in = JOptionPane.showInputDialog(null, "Enter damage modifier", "Attack", JOptionPane.QUESTION_MESSAGE);
						dmg_mod = Integer.parseInt(in);

						int roll = (int)(Math.random()*20+1);
						if(roll == 20)
						{
							JOptionPane.showMessageDialog(null, "Attack Roll: CRIT!"
									+ "\nDamage: " + (num_dice*dmg_die+dmg_mod),
									"CRIT!", JOptionPane.ERROR_MESSAGE);
						}
						else
						{
							int attack = roll + atk;
							int damage = dmg_mod;
							for(int i = 0; i < num_dice; i++)
								damage += (int)(Math.random()*dmg_die+1);

							JOptionPane.showMessageDialog(null, "Attack Roll: "+ attack
									+ "\nDamage Roll: " + damage, "Attack", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}
			});
		}

		public void paint(Graphics g)
		{
			Graphics2D g2 = (Graphics2D)g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.drawImage(background, 0, 0, this);
			g.setFont(font);
			if(combatants_initialized)
			{
				for(int i = 0; i < combatants.length; i++)
				{
					g.drawImage(combatant_frame, DISPLAY_HPOS-5, DISPLAY_VPOS+i*VDISTANCE_BETWEEN+OFFSET-5, this);
					g.drawString(combatants[i].getName(), DISPLAY_HPOS, DISPLAY_VPOS-1+i*VDISTANCE_BETWEEN);
					if(combatants[i] instanceof Enemy)
					{
						if(combatants[i].isDead())
							g.drawString("DEAD", DISPLAY_HPOS, DISPLAY_VPOS+TEXT_VSPACING-1+i*VDISTANCE_BETWEEN);
						else
							g.drawString("HP: " + combatants[i].getHealth(), DISPLAY_HPOS, DISPLAY_VPOS-1+TEXT_VSPACING+i*VDISTANCE_BETWEEN);
					}
				}
				for(int i = 0; i < attack_buttons.length; i++)
				{
					g.drawImage(attack_buttons[i].getImage(), attack_buttons[i].getHorizontalPosition(), attack_buttons[i].getVerticalPosition(), this);
				}
				for(int i = 0; i < take_damage_buttons.length; i++)
				{
					g.drawImage(take_damage_buttons[i].getImage(), take_damage_buttons[i].getHorizontalPosition(), take_damage_buttons[i].getVerticalPosition(), this);
				}
				g.drawImage(other_attack_button.getImage(), other_attack_button.getHorizontalPosition(), other_attack_button.getVerticalPosition(), this);
			}
		}

		public void actionPerformed(ActionEvent event)
		{
			repaint();
		}
	}

	private boolean isInteger(String s)
	{
		try
		{
			Integer.parseInt(s);
			return true;
		}
		catch(Exception e)
		{
			return false;
		}
	}
}

public class MapMaker extends JFrame
{
	static final int FRAME_HORIZONTAL_SIZE = 1300;
	static final int FRAME_VERTICAL_SIZE = 700;
	static final int HORIZONTAL_SIZE = FRAME_HORIZONTAL_SIZE - 100;
	static final int VERTICAL_SIZE = FRAME_VERTICAL_SIZE - 100;
	static Map MAP = new Map(HORIZONTAL_SIZE, VERTICAL_SIZE);
	final int TIMER_TIME = 50;
	Timer actorTimer = new Timer(TIMER_TIME, MAP);
	SaveButton savebutton1 = new SaveButton(FRAME_HORIZONTAL_SIZE-50, FRAME_VERTICAL_SIZE-100);
	LoadButton loadbutton1 = new LoadButton(FRAME_HORIZONTAL_SIZE-50, FRAME_VERTICAL_SIZE-140);
	EditButton editbutton1 = new EditButton(FRAME_HORIZONTAL_SIZE-50, FRAME_VERTICAL_SIZE-180);
	DMToggleButton dm1 = new DMToggleButton(MAP, FRAME_HORIZONTAL_SIZE-50, FRAME_VERTICAL_SIZE-220);
	PlaceMultipleButton pmb1 = new PlaceMultipleButton(FRAME_HORIZONTAL_SIZE-50, FRAME_VERTICAL_SIZE-260);
	EncounterButton en1 = new EncounterButton(FRAME_HORIZONTAL_SIZE-50, FRAME_VERTICAL_SIZE-300);
	MakeMonsterButton mm1 = new MakeMonsterButton(FRAME_HORIZONTAL_SIZE-50, FRAME_VERTICAL_SIZE-340);
	boolean placing_multiple = false;
	boolean first_selected = false;
	int place_horizontal1, place_horizontal2, place_vertical1, place_vertical2;
	Toolkit kit = Toolkit.getDefaultToolkit();
	Image background = kit.getImage(Constants.IMAGE_FOLDER + "\\stonefloor.png");

	Object placing_type = "Rectangle";

	JMenuBar map_menu;
	JMenu menu_file, menu_new, submenu_map;
	JMenuItem menu_item_grass, menu_item_water, menu_item_snow, menu_item_sand, menu_item_dirt;
	JMenuItem menu_item_monster, menu_item_encounter;
	JMenuItem menu_item_save, menu_item_load, menu_item_randomize;

    public static void main(String[] args)
    {
    	new MapMaker();
    }

	public MapMaker()
	{
		actorTimer.start();
		this.setSize(FRAME_HORIZONTAL_SIZE+50, FRAME_VERTICAL_SIZE);
    	this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	this.setTitle("MapMaker");
    	this.add(new Screen(), BorderLayout.CENTER);
    	this.setVisible(true);
    	this.setLocationRelativeTo(null);
    	this.setResizable(true);
//    	this.setResizable(false);

    	buildMenu();

    	this.setJMenuBar(map_menu);
	}

	private void buildMenu()
	{
		map_menu = new JMenuBar();

    	menu_file = new JMenu("File");
    	menu_file.setMnemonic(KeyEvent.VK_F);
    	menu_file.getAccessibleContext().setAccessibleDescription(
        			"Save/Load");

    	menu_new = new JMenu("New");
    	menu_new.setMnemonic(KeyEvent.VK_N);
    	menu_new.getAccessibleContext().setAccessibleDescription(
        			"New maps, monsters, etc.");

    	submenu_map = new JMenu("Map");
    	submenu_map.setMnemonic(KeyEvent.VK_M);
    	submenu_map.getAccessibleContext().setAccessibleDescription(
        			"Base maps");

    	menu_item_save = new JMenuItem("Save", KeyEvent.VK_S);
    	menu_item_load = new JMenuItem("Load", KeyEvent.VK_L);
    	menu_item_randomize = new JMenuItem("Randomize", KeyEvent.VK_R);

    	menu_item_monster = new JMenuItem("Monster", KeyEvent.VK_T);
    	menu_item_encounter = new JMenuItem("Encounter", KeyEvent.VK_E);

    	menu_item_dirt = new JMenuItem("Dirt", KeyEvent.VK_D);
    	menu_item_grass = new JMenuItem("Grass", KeyEvent.VK_G);
    	menu_item_sand = new JMenuItem("Sand", KeyEvent.VK_A);
    	menu_item_snow = new JMenuItem("Snow", KeyEvent.VK_O);
    	menu_item_water = new JMenuItem("Water", KeyEvent.VK_W);

    	map_menu.add(menu_file);
    	map_menu.add(menu_new);

    	menu_file.add(menu_item_save);
    	menu_file.add(menu_item_load);
    	menu_file.add(menu_item_randomize);

    	menu_new.add(submenu_map);
    	menu_new.add(menu_item_monster);
    	menu_new.add(menu_item_encounter);

    	submenu_map.add(menu_item_dirt);
    	submenu_map.add(menu_item_grass);
    	submenu_map.add(menu_item_sand);
    	submenu_map.add(menu_item_snow);
    	submenu_map.add(menu_item_water);

    	enableMenuUsability();
	}

	private void enableMenuUsability()
	{
		/*
			JMenuItem menu_item_grass, menu_item_water, menu_item_snow, menu_item_sand, menu_item_dirt;
			JMenuItem menu_item_monster, menu_item_encounter;
			JMenuItem menu_item_save, menu_item_load;
		*/
		menu_item_grass.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){

			}
		});
		menu_item_water.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){

			}
		});
		menu_item_snow.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){

			}
		});
		menu_item_sand.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){

			}
		});
		menu_item_dirt.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){

			}
		});
		menu_item_monster.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				new MonsterMaker();
			}
		});
		menu_item_encounter.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				new EncounterMaker();
			}
		});
		menu_item_save.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				MAP.save();
			}
		});
		menu_item_load.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				MAP.load();
			}
		});
		menu_item_randomize.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				MAP.randomize();
			}
		});
	}

	private class Screen extends JComponent implements ActionListener
	{
		Timer t;
		int oldxgrid = 0, oldygrid = 0;
		boolean first = true;
		public Screen()
		{
			t = new Timer(TIMER_TIME, this);
			t.start();
			this.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e)
			{
				if(!MAP.isEditing())
				{
					if(e.getButton() == MouseEvent.BUTTON3)
					{
						MAP.checkMouseOver(e.getX(), e.getY());
					}
					else if(e.getButton() == MouseEvent.BUTTON2)
					{
						MAP.editObject(e.getX(), e.getY());
					}
					else if(e.getButton() == MouseEvent.BUTTON1)
					{
						if(e.getX() < MAP.getHorSize() && e.getY() < MAP.getVerSize())
						{
							if(!placing_multiple)
								MAP.placeObject(e.getX()/20, e.getY()/20);
							else
							{
								if(!first_selected)
								{
									place_horizontal1 = e.getX()/20;
									place_vertical1 = e.getY()/20;
									first_selected = true;
								}
								else if(first_selected)
								{
									place_horizontal2 = e.getX()/20;
									place_vertical2 = e.getY()/20;
									first_selected = false;

									if(place_horizontal1 > place_horizontal2)
									{
										int tmp = place_horizontal1;
										place_horizontal1 = place_horizontal2;
										place_horizontal2 = tmp;
									}
									if(place_vertical1 > place_vertical2)
									{
										int tmp = place_vertical1;
										place_vertical1 = place_vertical2;
										place_vertical2 = tmp;
									}

									if(placing_type.equals("Rectangle"))
										MAP.placeMultiple(place_horizontal1, place_vertical1, place_horizontal2, place_vertical2);
									else
										MAP.fillEllipse(place_horizontal1, place_vertical1, place_horizontal2, place_vertical2);
									MAP.clearDrawGrid();
									placing_multiple = false;
								}
							}
						}
						else if(e.getX() > MAP.getHUDHPos() && e.getX() < MAP.getHUDHPos() + MAP.getHUDHDim() && e.getY() > MAP.getHUDVPos() && e.getY() < MAP.getHUDVPos() + MAP.getHUDVDim())
						{
							MAP.selectTerrain(e.getX()/20, e.getY()/20);
						}
						else if(savebutton1.intersects(e.getX(), e.getY()))
						{
							MAP.save();
						}
						else if(loadbutton1.intersects(e.getX(), e.getY()))
						{
							MAP.load();
						}
						else if(editbutton1.intersects(e.getX(), e.getY()))
						{
							MAP.edit();
						}
						else if(dm1.intersects(e.getX(), e.getY()))
						{
							MAP.toggleDMMode();
							dm1.setImage();
						}
						else if(en1.intersects(e.getX(), e.getY()))
						{
							new EncounterMaker();
						}
						else if(mm1.intersects(e.getX(), e.getY()))
						{
							new MonsterMaker();
						}
						else if(pmb1.intersects(e.getX(), e.getY()))
						{
							Object[] types = {"Rectangle", "Circle"};
							placing_type = JOptionPane.showInputDialog(null, "What shape would you like to place?", "Placing Multiple...", JOptionPane.QUESTION_MESSAGE, null, types, types[0]);
							if(placing_type != null)
								placing_multiple = true;
						}
						else
						{
							MAP.clearSelectedTerrain();
						}
					}
				}
				else
				{
					MAP.editObject(e.getX(), e.getY());
				}
			}
			});
			this.addMouseMotionListener(new MouseMotionListener(){
				public void mouseMoved(MouseEvent e)
				{
					if(e.getX() < MAP.getHorSize() && e.getY() < MAP.getVerSize())
					{
						if(!placing_multiple)
						{
							int x = e.getX()/20, y = e.getY()/20;
							if(x != oldxgrid || y != oldygrid)
							{
								MAP.removeFromDraw(oldxgrid, oldygrid);
								String selected = MAP.getSelectedTerrain();
								if(!selected.equals(""))
								{
									HUD_Object h = null;
									if(selected.equals(Grass.CODE))
										h = new HUD_Grass(MAP, e.getX(), e.getY());
									else if(selected.equals(Dirt.CODE))
										h = new HUD_Dirt(MAP, e.getX(), e.getY());
									else if(selected.equals(Water.CODE))
										h = new HUD_Water(MAP, e.getX(), e.getY());
									else if(selected.equals(Sand.CODE))
										h = new HUD_Sand(MAP, e.getX(), e.getY());
									else if(selected.equals(Snow.CODE))
										h = new HUD_Snow(MAP, e.getX(), e.getY());
									else if(selected.equals(Mountain.CODE))
										h = new HUD_Mountain(MAP, e.getX(), e.getY());
									else if(selected.equals(Tree.CODE))
										h = new HUD_Tree(MAP, e.getX(), e.getY());
									else if(selected.equals(City.CODE))
										h = new HUD_City(MAP, e.getX(), e.getY());
									else if(selected.equals(Fortress.CODE))
										h = new HUD_Fortress(MAP, e.getX(), e.getY());
									else if(selected.equals(Town.CODE))
										h = new HUD_Town(MAP, e.getX(), e.getY());
									else if(selected.equals(Camp.CODE))
										h = new HUD_Camp(MAP, e.getX(), e.getY());
									else if(selected.equals(Landmark.CODE))
										h = new HUD_Landmark(MAP, e.getX(), e.getY());
									MAP.addToDraw(h, x, y);
								}
								oldxgrid = x;
								oldygrid = y;
							}
						}
						else
						{
							if(first_selected)
							{
								int x1 = place_horizontal1;
								int y1 = place_vertical1;
								int x2 = e.getX()/20;
								int y2 = e.getY()/20;
								if(x1 > x2)
								{
									int tmp = x1;
									x1 = x2;
									x2 = tmp;
								}
								if(y1 > y2)
								{
									int tmp = y1;
									y1 = y2;
									y2 = tmp;
								}
								MAP.addMultipleDraw(x1, y1, x2, y2, (String)placing_type);
							}
							else
							{
								int x = e.getX()/20, y = e.getY()/20;
								if(x != oldxgrid || y != oldygrid)
								{
									MAP.removeFromDraw(oldxgrid, oldygrid);
									String selected = MAP.getSelectedTerrain();
									if(!selected.equals(""))
									{
										HUD_Object h = null;
										if(selected.equals(Grass.CODE))
											h = new HUD_Grass(MAP, e.getX(), e.getY());
										else if(selected.equals(Dirt.CODE))
											h = new HUD_Dirt(MAP, e.getX(), e.getY());
										else if(selected.equals(Water.CODE))
											h = new HUD_Water(MAP, e.getX(), e.getY());
										else if(selected.equals(Sand.CODE))
											h = new HUD_Sand(MAP, e.getX(), e.getY());
										else if(selected.equals(Snow.CODE))
											h = new HUD_Snow(MAP, e.getX(), e.getY());
										else if(selected.equals(Mountain.CODE))
											h = new HUD_Mountain(MAP, e.getX(), e.getY());
										else if(selected.equals(Tree.CODE))
											h = new HUD_Tree(MAP, e.getX(), e.getY());
										else if(selected.equals(City.CODE))
											h = new HUD_City(MAP, e.getX(), e.getY());
										else if(selected.equals(Fortress.CODE))
											h = new HUD_Fortress(MAP, e.getX(), e.getY());
										else if(selected.equals(Town.CODE))
											h = new HUD_Town(MAP, e.getX(), e.getY());
										else if(selected.equals(Camp.CODE))
											h = new HUD_Camp(MAP, e.getX(), e.getY());
										else if(selected.equals(Landmark.CODE))
											h = new HUD_Landmark(MAP, e.getX(), e.getY());
										MAP.addToDraw(h, x, y);
									}
									oldxgrid = x;
									oldygrid = y;
								}
							}
						}
					}
					else
					{
						MAP.clearDrawGrid();
					}
				}
				public void mouseDragged(MouseEvent e)
				{
				}
			});
		}
		public void paint(Graphics g)
		{
			int startX = 0, startY = 0;
			Graphics2D g2 = (Graphics2D)g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			g.drawImage(background, 0, 0, this);

			for(int i = 0; i < MAP.getHorizSize(); i++)
			{
				for(int j = 0; j < MAP.getVertSize(); j++)
				{
					if(MAP.getTerrain(i,j) != null)
						g.drawImage(MAP.getTerrain(i,j).getImage(), startX+20*i, startY+20*j, this);
					if(MAP.getDrawObject(i, j) != null)
						g.drawImage(MAP.getDrawObject(i,j).getImage(), startX+20*i, startY+20*j, this);
				}
			}
			for(int i = 0; i < MAP.getHorizSize(); i++)
			{
				for(int j = 0; j < MAP.getVertSize(); j++)
				{
					if(MAP.getObject(i,j) != null)
						g.drawImage(MAP.getObject(i,j).getImage(), startX+MAP.getObject(i,j).getHPos(), startY+MAP.getObject(i,j).getVPos(), this);
				}
			}
			for(int i = 0; i < 4; i++)
			{
				for(int j = 0; j < 4; j++)
				{
					if(MAP.getHUDObject(i,j) != null)
						g.drawImage(MAP.getHUDObject(i,j).getImage(), MAP.getHUDObject(i,j).getHPos(), MAP.getHUDObject(i,j).getVPos(), this);
				}
			}
			g.drawImage(savebutton1.getImage(), savebutton1.getHorizontalPosition(), savebutton1.getVerticalPosition(), this);
			g.drawImage(loadbutton1.getImage(), loadbutton1.getHorizontalPosition(), loadbutton1.getVerticalPosition(), this);
			g.drawImage(editbutton1.getImage(), editbutton1.getHorizontalPosition(), editbutton1.getVerticalPosition(), this);
			g.drawImage(dm1.getImage(), dm1.getHorizontalPosition(), dm1.getVerticalPosition(), this);
			g.drawImage(en1.getImage(), en1.getHorizontalPosition(), en1.getVerticalPosition(), this);
			g.drawImage(pmb1.getImage(), pmb1.getHorizontalPosition(), pmb1.getVerticalPosition(), this);
			g.drawImage(mm1.getImage(), mm1.getHorizontalPosition(), mm1.getVerticalPosition(), this);
		}
		public void actionPerformed(ActionEvent event)
		{
			repaint();
		}
	}
}