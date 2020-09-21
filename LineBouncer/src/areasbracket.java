import java.awt.*;
import java.applet.Applet;

public class areasbracket extends Applet implements Runnable
{
  Dimension	dimension;
  Font 		BigFont = new Font("Helvetica", Font.BOLD, 25);
  Font		smallfont = new Font("Helvetica", Font.BOLD, 15);
  Font		scorefont = new Font("Helvetica", Font.BOLD, 20);
  FontMetrics	fmsmall, fmlarge;  
  Graphics	goff;
  Image		image;
  Thread	thethread;

  boolean	ingame=false;
  boolean	showtitle=true;
  int		score;
  Color	frame=new Color(255,64,128);
  Color	frameafter=new Color(243,255,58);
  Color	fillcolor=new Color(64,64,160);
  Color	enemycolor1=new Color(255,19,19);
  Color	mycolor=new Color(128,192,192);

  final int	screendelay=120;
  int count=screendelay;
  final int	halfblockwidth=8;
  final int halfblockheight=8;
  final	int	blockwidth=halfblockwidth*2;
  final int	blockheight=halfblockheight*2;
  final int	squarewidth=4;
  final int	xsquares=5;
  final int	xblocks=xsquares*squarewidth+1;
  final int	yblocks=30+1;
  final int	scoreheight=32;
  final int	screenwidth=blockwidth*xblocks;
  final int	screenheight=blockheight*yblocks;

  int[][]	screendata;
  int[][]	colordata;
  int		level1[]= {
                  0,0,6,0,	4,0,6,0,	8,0,6,0,	12,0,6,0,	16,0,6,0,
 		  0,6,6,0,	4,6,6,0,	8,6,6,0,	12,6,6,0,	16,6,6,0,
		  0,12,6,0,	4,12,6,0,	8,12,6,0,	12,12,6,0,	16,12,6,0,
		  0,18,6,0,	4,18,6,0,	8,18,6,0,	12,18,6,0,	16,18,6,0,
		  0,24,6,0,	4,24,6,0,	8,24,6,0,	12,24,6,0,	16,24,6,0,
		  -1,-1,-1,-1
		};

  int[]		level;
  final	int	maxenemies=8;
  int		enemiesnum=4;
  int		enemyspeed=2;
  int[]		enemydx, enemydy, enemyx, enemyy, enemyreqdx, enemyreqdy;
  int		myx,myy,mydx,mydy,myreqx,myreqy;
  final int myspeed=4;
  int		lives;
  final int	maxlives=3;
  boolean	completed,die;
  int		xblock=0;
  int		deathposition=0;
  final int	deathdelay=3;
  int		deathcount=0;

  public void init()
  {
    Graphics g;
    short i;
    resize(screenwidth, screenheight+blockheight*2);
    dimension = size();

    if (goff==null && dimension.width>0 && dimension.height>0)
    {
      image = createImage(dimension.width, dimension.height);
      goff = image.getGraphics();
    }

    setBackground(Color.black);
    g=getGraphics();
    g.setFont(smallfont);
    fmsmall = g.getFontMetrics();
    g.setFont(BigFont);
    fmlarge = g.getFontMetrics();

    screendata=new int[xblocks][yblocks];
    colordata=new int[xblocks][yblocks];

    enemyx=new int[maxenemies];
    enemyy=new int[maxenemies];
    enemydx=new int[maxenemies];
    enemydy=new int[maxenemies];
    enemyreqdx=new int[maxenemies];
    enemyreqdy=new int[maxenemies];
    level=level1;
    GameInit();
  }


  public void GameInit()
  {
    score=0;
    lives=maxlives;
    LevelInit();
  }

  public void LevelInit()
  {
    int i=0,j;
    int x,y,ysize;

    completed=false;
    die=false;

    for (x=0; x<xblocks; x++)
    {
      for (y=0; y<yblocks; y++)
      {
        screendata[x][y]=0;
        colordata[x][y]=0;
      }
    }

    while (level[i]>=0)
    {
      x=level[i];
      y=level[i+1];
      ysize=level[i+2];
      level[i+3]=0;

      // first the corners
      screendata[x][y]|=6;
      screendata[x+squarewidth][y]|=12;
      screendata[x+squarewidth][y+ysize]|=9;
      screendata[x][y+ysize]|=3;
      for (j=y+1; j<(y+ysize); j++)
      {
        screendata[x][j]|=5;
        screendata[x+squarewidth][j]|=5;
      }
      for (j=x+1; j<(x+squarewidth); j++)
      {
        screendata[j][y]|=10;
        System.out.print("");  // Need this because otherwise IE4.0 hangs (???)
                               // Netscape rulez
        screendata[j][y+ysize]|=10;
      }
      i+=4;
    }
    LevelContinue();
  }

  public void LevelContinue()
  {
    int		i;
    int		x=0;

    for (i=0; i<enemiesnum; i++)
    {
      enemyy[i]=((int)(Math.random()*blockheight*(yblocks/2))/enemyspeed)*enemyspeed;
      enemyx[i]=(x/enemyspeed)*enemyspeed;
      x+=squarewidth*blockwidth;
      if (x>=xblocks*blockwidth)
        x=0;
      enemydy[i]=1;
      enemydx[i]=0;
      enemyreqdy[i]=1;
      if (Math.random()<0.5)
        enemyreqdx[i]=-1;
      else
        enemyreqdx[i]=1;

      myx=xblocks*blockwidth/2-halfblockwidth;
      myy=(yblocks-1)*blockheight;
      mydx=0;
      mydy=0;
      myreqx=0;
      myreqy=0;
    }
  }


  public boolean keyDown(Event e, int key)
  {
    if (ingame)
    {
      if (key == Event.LEFT)
      {
        myreqx=-1;
        myreqy=0;
      }
      else if (key == Event.RIGHT)
      {
        myreqx=1;
        myreqy=0;
      }
      else if (key == Event.UP)
      {
        myreqy=-1;
        myreqx=0;
      }
      else if (key == Event.DOWN)
      {
        myreqy=1;
        myreqx=0;
      }
      else if (key == Event.ESCAPE)
      {
        ingame=false;
      }
    }
    else
    {
      if (key == 's' || key == 'S')
      {
        ingame=true;
        GameInit();
      }
    }
    return true;
  }
  

  public void paint(Graphics g)
  {
    String s;
    Graphics gg;

    if (goff==null || image==null)
      return;

    goff.setColor(Color.black);
    goff.fillRect(0, 0, dimension.width, dimension.height);

    DrawPlayField();
    DrawScore();
    if (completed)
    {
      LevelCompleted();
    }
    else
    {
      DrawEnemies();
      if (die)
      {
        Death();
      }
      else
      {
        MoveEnemies();
        if (ingame)
          PlayGame();
        else
          PlayDemo();
      }
    }
    g.drawImage(image, 0, 0, this);
  }

  public boolean keyUp(Event e, int key)
  {
    if (key == Event.LEFT || key == Event.RIGHT || key == Event.UP ||  key == Event.DOWN)
    {
    }
    return true;
  }

  
  public void PlayGame()
  {
    DrawMe();
    MoveMe();
  }


  public void PlayDemo()
  {
    ShowIntroScreen();
  }


  public void DrawPlayField()
  {
    int 	i,j;
    int 	x,y;
    int 	ch;

    x=0;
    for (i=0; i<xblocks; i++)
    {
      y=0;
      for (j=0; j<yblocks; j++)
      {
        if (colordata[i][j]==0)
        {
          goff.setColor(frame);
        }
        else
        {
          goff.setColor(frameafter);
        }
        ch=screendata[i][j];
        if ((ch&1)!=0)
        {
          goff.fillRect(x+halfblockwidth-1,y,2,halfblockheight);
        }
        if ((ch&2)!=0)
        {
          goff.fillRect(x+halfblockwidth,y+halfblockheight-1,halfblockwidth,2);
        }
        if ((ch&4)!=0)
        {
          goff.fillRect(x+halfblockwidth-1,y+halfblockheight,2,halfblockheight);
        }
        if ((ch&8)!=0)
        {
          goff.fillRect(x,y+halfblockheight-1,halfblockwidth,2);
        }
        y+=blockheight;
      }
      x+=blockwidth;
    }
    CheckFills();
  }


  public void DrawMe()
  {
    goff.setColor(mycolor);
    goff.fillRect(myx,myy,blockwidth,blockheight);
  }


  public void Death()
  {
    DrawMe();
    goff.setColor(Color.black);
    goff.fillRect(myx, myy+halfblockheight-deathposition,
                  blockwidth, deathposition*2);
    deathcount++;
    if (deathcount>deathdelay)
    {
      deathcount=0;
      deathposition++;
      if (deathposition>halfblockheight)
      {
        deathposition=0;
        lives--;
        if (lives<=0)
        {
          ingame=false;
        }
        die=false;
        LevelContinue();
      }
    }
  }


  public void MoveMe()
  {
    int x=myx/blockwidth;
    int y=myy/blockheight;
    int scrdat=screendata[x][y];

    if ((myreqx==-mydx && mydx!=0) || (myreqy==-mydy && mydy!=0))
    {
      mydx=myreqx;
      mydy=myreqy;
    }
    if ((myx%blockwidth)==0 && (myy%blockheight)==0)
    {
      if (myreqx==1 && (scrdat&2)!=0)
      {
        mydx=1;
        mydy=0;
      }
      if (myreqx==-1 && (scrdat&8)!=0)
      {
        mydx=-1;
        mydy=0;
      }
      if (myreqy==1 && (scrdat&4)!=0)
      {
        mydx=0;
        mydy=1;
      }
      if (myreqy==-1 && (scrdat&1)!=0)
      {
        mydx=0;
        mydy=-1;
      }
    }

    myx+=mydx*myspeed;
    myy+=mydy*myspeed;
    if (myx<0) myx=0;
    if (myx>(xblocks-1)*blockwidth) myx=(xblocks-1)*blockwidth;
    if (myy<0) myy=0;
    if (myy>(yblocks-1)*blockheight) myy=(yblocks-1)*blockheight;
    x=(myx+halfblockwidth)/blockwidth;
    y=(myy+halfblockheight)/blockheight;
    if (colordata[x][y]==0)
    {
      score+=10;
      colordata[x][y]=1;
    }
  }


  public void DrawEnemies()
  {
    int		i;

    goff.setColor(enemycolor1);

    for (i=0; i<enemiesnum; i++)
    {
      goff.fillRect(enemyx[i],enemyy[i],blockwidth,blockheight);
      if (myx>(enemyx[i]-halfblockwidth) && myx<(enemyx[i]+halfblockwidth) &&
          myy>(enemyy[i]-halfblockheight) && myy<(enemyy[i]+halfblockheight) &&
          ingame)
      {
        die=true;
      }
    }    
  }


  public void MoveEnemies()
  {
    int		i;
    int		x,y;
    int		olddx,olddy,scrdat;

    for (i=0; i<enemiesnum; i++)
    {
      x=enemyx[i]/blockwidth;
      y=enemyy[i]/blockheight;
      olddx=enemydx[i];
      olddy=enemydy[i];

      if (enemyx[i]==0)
      {
        enemyreqdx[i]=1;
      }
      else if (enemyx[i]==(xblocks-1)*blockwidth)
      {
        enemyreqdx[i]=-1;
      }
      if (enemyy[i]==0)
      {
        enemyreqdy[i]=1;
      }
      else if (enemyy[i]==(yblocks-1)*blockheight)
      {
        enemyreqdy[i]=-1;
      }

      scrdat=screendata[x][y];

      if ((enemyx[i]%blockwidth)==0 && (enemyy[i]%blockheight)==0)
      {
        if (olddx==1 || olddx==-1)
        {
          if ((scrdat&1)!=0 && enemyreqdy[i]==-1)
          {
            enemydx[i]=0;
            enemydy[i]=-1;
          }
          else if ((scrdat&4)!=0 && enemyreqdy[i]==1)
          {
            enemydx[i]=0;
            enemydy[i]=1;
          }
          else if (olddx==1 && (scrdat&2)==0)
          {
            enemydx[i]=-1;
          }
          else if (olddx==-1 && (scrdat&8)==0)
          {
            enemydx[i]=1;
          }
        }
        else if (olddy==1 || olddy==-1)
        {
          if ((scrdat&2)!=0 && enemyreqdx[i]==1)
          {
            enemydx[i]=1;
            enemydy[i]=0;
          }
          else if ((scrdat&8)!=0 && enemyreqdx[i]==-1)
          {
            enemydx[i]=-1;
            enemydy[i]=0;
          }
          else if (olddy==1 && (scrdat&4)==0)
          {
            enemydy[i]=-1;
          }
          else if (olddy==-1 && (scrdat&1)==0)
          {
            enemydy[i]=1;
          }
       }
      }
      enemyx[i]+=enemydx[i]*enemyspeed;
      enemyy[i]+=enemydy[i]*enemyspeed;
    }
  }


  public void CheckFills()
  {
    int		i=0,j;
    boolean	dofill;
    int		x,y,ysize,filled;
    boolean     allfilled=true;

    goff.setColor(fillcolor);

    while(level[i]>=0)
    {
      dofill=true;
      x=level[i];
      y=level[i+1];
      ysize=level[i+2];
      filled=level[i+3];

      if (filled==0)
      {
        for (j=x; dofill && (j<x+squarewidth); j++)
        {
          dofill&=(colordata[j][y]!=0) & (colordata[j][y+ysize]!=0);
        }
        for (j=y+1; dofill && (j<y+ysize); j++)
        {
          dofill&=(colordata[x][j]!=0) & (colordata[x+squarewidth][j]!=0);
        }
        if (dofill)
        {
          score+=100;
          level[i+3]=1;
        }
      }

      if (dofill || filled!=0)
      {
        goff.fillRect(x*blockwidth+halfblockwidth+2,y*blockheight+halfblockheight+2,
			squarewidth*blockwidth-4,
			ysize*blockheight-4);
      }
      else
      {
        allfilled=false;
      }
      i+=4;
    }
    if (allfilled)
    {
      completed=true;
    }
  }


  public void LevelCompleted()
  {
    goff.setColor(Color.black);
    goff.fillRect(0, 0, xblock, yblocks*blockheight-1);
    goff.fillRect(xblocks*blockwidth-1-xblock,0,
                  xblocks*blockwidth-1,yblocks*blockheight-1);
    xblock+=2;
    if (xblock>(xblocks*blockwidth/2))
    {
      completed=false;
      xblock=0;

      if (enemyspeed==2)
      {
        enemyspeed=4;
      }
      else
      {
        if (enemiesnum<maxenemies)
        {
         enemiesnum++;
        }
        else
        {
          enemyspeed=8;
        }
      }
     
      LevelInit();
    }
  }


  public void ShowIntroScreen()
  {
    String s;
    if (showtitle)
    {
      s="welcome to areas bracket";
      goff.setFont(smallfont);
      goff.setColor(new Color(16,79,151));
      goff.drawString(s,(screenwidth-fmsmall.stringWidth(s))/2,screenheight/2 );

    }
    else
    {
      goff.setFont(smallfont);
      goff.setColor(new Color(16,79,151));
      s="'S' to start game";
      goff.drawString(s,(screenwidth-fmsmall.stringWidth(s))/2,screenheight/2 - 10);
      goff.setColor(new Color(16,79,151));
      s="Use cursor keys to move";
      goff.drawString(s,(screenwidth-fmsmall.stringWidth(s))/2,screenheight/2 + 20);
    }
    count--;
    if (count<=0)
    { count=screendelay; showtitle=!showtitle; }
  }


  public void DrawScore()
  {
    int i,x,y;
    String s;

    goff.setFont(scorefont);
    goff.setColor(new Color(16,79,151));
    s="Score: "+score;
    goff.drawString(s,blockwidth,(yblocks+1)*blockheight);

    
    goff.setColor(new Color(16,79,151));
    s="lives:" +lives;
    goff.drawString(s,(screenwidth-fmsmall.stringWidth(s))-20,(yblocks+1)*blockheight);

  }


  public void run()
  {
    long  starttime;
    Graphics g;
    Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
    g=getGraphics();

    while(true)
    {
      starttime=System.currentTimeMillis();
      try
      {
        paint(g);
        starttime += 40;
        Thread.sleep(Math.max(0, starttime-System.currentTimeMillis()));
      }
      catch (InterruptedException e)
      {
        break;
      }
    }
  }

  public void start()
  {
    if (thethread == null) {
      thethread = new Thread(this);
      thethread.start();
    }
  }

  public void stop()
  {
    if (thethread != null) {
      thethread.stop();
      thethread = null;
    }
  }
}
