package com.javabinary;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class Main {
    static class Example extends HakimenGameEngine {
        String[] tetrominos = new String[7];
        BufferedImage[] skin = new BufferedImage[5];

        final int nFieldWidth = 12;
        final int nFieldHeight = 18;
        char[] pField;

        int selectedSkin;
        {
            try {
                selectedSkin = Integer.parseInt(new BufferedReader(new FileReader("skin.txt")).readLine());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //Game State

        int nCurrentPiece = ThreadLocalRandom.current().nextInt(0,7);
        int nCurrentRotation = 0;
        int nCurrentX = (nFieldWidth-3)/ 2;
        int nCurrentY = 0;

        int nSpeed = 20;
        int nSpeedCounter = 0;
        boolean bForceDown = false;
        boolean bGameOver = false;
        int nPieceCount = 0;
        int nScore;
        int nStoredPiece = -1;

        boolean bAlreadyStored;
        boolean bHoldStore;
        boolean bHoldRotate = false;

        ArrayList<Integer> vLines = new ArrayList<>();

        @Override
        public boolean OnUserCreate() {
            //Assets
            System.out.println(selectedSkin);
            tetrominos[0] =  "..X.";
            tetrominos[0] += "..X.";
            tetrominos[0] += "..X.";
            tetrominos[0] += "..X.";

            tetrominos[1] =  "..X.";
            tetrominos[1] += ".XX.";
            tetrominos[1] += ".X..";
            tetrominos[1] += "....";

            tetrominos[2] =  ".X..";
            tetrominos[2] += ".XX.";
            tetrominos[2] += "..X.";
            tetrominos[2] += "....";

            tetrominos[3] =  "....";
            tetrominos[3] += ".XX.";
            tetrominos[3] += ".XX.";
            tetrominos[3] += "....";

            tetrominos[4] =  "..X.";
            tetrominos[4] += ".XX.";
            tetrominos[4] += "..X.";
            tetrominos[4] += "....";

            tetrominos[5] =  "....";
            tetrominos[5] += ".XX.";
            tetrominos[5] += "..X.";
            tetrominos[5] += "..X.";

            tetrominos[6] =  "....";
            tetrominos[6] += ".XX.";
            tetrominos[6] += ".X..";
            tetrominos[6] += ".X..";

            pField = new char[nFieldWidth * nFieldHeight]; //Instantiate the field char Array
            for (int x = 0; x < nFieldWidth; x++) {
                for (int y = 0; y < nFieldHeight; y++) {
                    pField[y * nFieldWidth + x] = (x == 0 || x == nFieldWidth - 1 || y == nFieldHeight-1) ? '#' : ' ';
                }
            }
            //Load texture of the Blocks
            try {
                skin[0] = ImageIO.read(getClass().getResource("assets/skin1.png"));
                skin[1] = ImageIO.read(getClass().getResource("assets/skin2.png"));
                skin[2] = ImageIO.read(getClass().getResource("assets/skin3.png"));
                skin[3] = ImageIO.read(getClass().getResource("assets/skin4.png"));
                skin[4] = ImageIO.read(getClass().getResource("assets/skin5.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        public boolean OnUserUpdate(Graphics2D g, int elapsedTime) {
            g.scale(2,2);
            FillRect(g, 0, 0, ScreenWidth(), ScreenHeight(), Color.BLACK);
            //Kill the Game
            if(bGameOver) {
                try {
                    Thread.sleep(2000);
                    return false;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //Timing
            try {
                Thread.sleep(40);
                nSpeedCounter++;
                bForceDown = (nSpeedCounter == nSpeed);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            //Input and Logic
            nCurrentX -= (b_keys[KeyEvent.VK_LEFT] && DoesPieceFit(nCurrentPiece, nCurrentRotation, nCurrentX - 1, nCurrentY)) ? 1 : 0;
            nCurrentX += (b_keys[KeyEvent.VK_RIGHT] && DoesPieceFit(nCurrentPiece, nCurrentRotation, nCurrentX + 1, nCurrentY)) ? 1 : 0;
            nCurrentY += (b_keys[KeyEvent.VK_DOWN] && DoesPieceFit(nCurrentPiece, nCurrentRotation, nCurrentX, nCurrentY + 1))  ? 1 : 0;

            if(b_keys[KeyEvent.VK_Z]) {
                nCurrentRotation+=(!bHoldRotate && DoesPieceFit(nCurrentPiece, nCurrentRotation+1, nCurrentX, nCurrentY)) ? 1 : 0;
                bHoldRotate = true;
            }else bHoldRotate = false;

            if(b_keys[KeyEvent.VK_C] && !bAlreadyStored && !bHoldStore){
               if(nStoredPiece == -1) {
                   nStoredPiece = nCurrentPiece;
                   nCurrentX = (nFieldWidth-3)/2;
                   nCurrentRotation = 0;
                   nCurrentY = 0;
                   nCurrentPiece = ThreadLocalRandom.current().nextInt(0,7);
                   bAlreadyStored = true;
                   bHoldStore = true;
               }else{
                   int cPiece = nCurrentPiece;
                   nCurrentPiece = nStoredPiece;
                   nStoredPiece = cPiece;
                   nCurrentX = (nFieldWidth-3)/2;
                   nCurrentRotation = 0;
                   nCurrentY = 0;
                   bAlreadyStored = true;
               }
            }else
                bHoldStore = false;



            if (bForceDown){
                if(DoesPieceFit(nCurrentPiece,nCurrentRotation,nCurrentX,nCurrentY+1)){
                    nCurrentY++;
                }
                else{
                    //Lock piece in the Field
                    for (int px = 0; px < 4; px++) {
                        for (int py = 0; py < 4; py++) {
                            if(tetrominos[nCurrentPiece].charAt(Rotate(px,py,nCurrentRotation)) == 'X') {
                                switch (nCurrentPiece)
                                {
                                    case 0:
                                        pField[(nCurrentY+py)*nFieldWidth+(nCurrentX+px)] = 'A';
                                        break;
                                    case 1:
                                        pField[(nCurrentY+py)*nFieldWidth+(nCurrentX+px)] = 'B';
                                        break;
                                    case 2:
                                        pField[(nCurrentY+py)*nFieldWidth+(nCurrentX+px)] = 'C';
                                        break;
                                    case 3:
                                        pField[(nCurrentY+py)*nFieldWidth+(nCurrentX+px)] = 'D';
                                        break;
                                    case 4:
                                        pField[(nCurrentY+py)*nFieldWidth+(nCurrentX+px)] = 'E';
                                        break;
                                    case 5:
                                        pField[(nCurrentY+py)*nFieldWidth+(nCurrentX+px)] = 'F';
                                        break;
                                    case 6:
                                        pField[(nCurrentY+py)*nFieldWidth+(nCurrentX+px)] = 'G';
                                        break;
                                }
                            }
                        }
                    }

                    nPieceCount++;
                    if(nPieceCount % 10 == 0) {
                        if(nSpeed >= 10) nSpeed--;
                    }
                    //Check if there is any lines
                    for (int py = 0; py < 4; py++) {
                        boolean bLine = true;
                        if (nCurrentY+py<nFieldHeight-1) {
                            for (int px = 1; px < nFieldWidth-1; px++) {
                                bLine = (pField[(nCurrentY+py)*nFieldWidth+px]) != ' ' & bLine;
                            }
                            if (bLine){
                                //Remove Line
                                for (int px = 1; px < nFieldWidth-1; px++) {
                                    pField[(nCurrentY+py)*nFieldWidth+px] = ' ';
                                }
                                vLines.add(nCurrentY+py);
                            }
                        }
                    }
                    nScore+=25;
                    if(!vLines.isEmpty()) nScore += (1<<vLines.size())*100;
                    bAlreadyStored = false;
                    //Choose next Piece
                    nCurrentX = (nFieldWidth-3)/2;
                    nCurrentY = 0;
                    nCurrentPiece = ThreadLocalRandom.current().nextInt(0,7);
                    nCurrentRotation = 0;
                    nSpeedCounter = 0;
                    //If doesn't fit
                    bGameOver = !DoesPieceFit(nCurrentPiece,nCurrentRotation,nCurrentX,nCurrentY);

                }
                nSpeedCounter = 0;
            }

            //Draw Field
            for (int x = 0; x < nFieldWidth; x++) {
                for (int y = 0; y < nFieldHeight; y++) {
                    if (pField[x + nFieldWidth * y] == '#') {
                        DrawPartialImage(g, 16 + x * 16, 32 + y * 16, 7*16, 0, 16, 16, skin[selectedSkin]);
                    }else if (pField[x + nFieldWidth * y] == 'A') {
                        DrawPartialImage(g, 16 + x * 16, 32 + y * 16, 3*16, 0, 16, 16, skin[selectedSkin]);
                    }else if (pField[x + nFieldWidth * y] == 'B') {
                        DrawPartialImage(g, 16 + x * 16, 32 + y * 16, 6*16, 0, 16, 16, skin[selectedSkin]);
                    }else if (pField[x + nFieldWidth * y] == 'C') {
                        DrawPartialImage(g, 16 + x * 16, 32 + y * 16, 0*16, 0, 16, 16, skin[selectedSkin]);
                    }else if (pField[x + nFieldWidth * y] == 'D') {
                        DrawPartialImage(g, 16 + x * 16, 32 + y * 16, 4*16, 0, 16, 16, skin[selectedSkin]);
                    }else if (pField[x + nFieldWidth * y] == 'E') {
                        DrawPartialImage(g, 16 + x * 16, 32 + y * 16, 5*16, 0, 16, 16, skin[selectedSkin]);
                    }else if (pField[x + nFieldWidth * y] == 'F') {
                        DrawPartialImage(g, 16 + x * 16, 32 + y * 16, 1*16, 0, 16, 16, skin[selectedSkin]);
                    }else if (pField[x + nFieldWidth * y] == 'G') {
                        DrawPartialImage(g, 16 + x * 16, 32 + y * 16, 2*16, 0, 16, 16, skin[selectedSkin]);
                    }
                }
            }
            //Draw Current Piece
            for (int px = 0; px < 4; px++) {
                for (int py = 0; py < 4; py++) {
                    if (tetrominos[nCurrentPiece].charAt(Rotate(px, py, nCurrentRotation)) == 'X') {
                        switch (nCurrentPiece) {
                            case 0:
                                DrawPartialImage(g, 16 + (nCurrentX + px) * 16, 32 + (nCurrentY + py) * 16, 3*16, 0, 16, 16, skin[selectedSkin]);
                                break;
                            case 1:
                                DrawPartialImage(g, 16 + (nCurrentX + px) * 16, 32 + (nCurrentY + py) * 16, 6*16, 0, 16, 16, skin[selectedSkin]);
                                break;
                            case 2:
                                DrawPartialImage(g, 16 + (nCurrentX + px) * 16, 32 + (nCurrentY + py) * 16, 0*16, 0, 16, 16, skin[selectedSkin]);
                                break;
                            case 3:
                                DrawPartialImage(g, 16 + (nCurrentX + px) * 16, 32 + (nCurrentY + py) * 16, 4*16, 0, 16, 16, skin[selectedSkin]);
                                break;
                            case 4:
                                DrawPartialImage(g, 16 + (nCurrentX + px) * 16, 32 + (nCurrentY + py) * 16, 5*16, 0, 16, 16, skin[selectedSkin]);
                                break;
                            case 5:
                                DrawPartialImage(g, 16 + (nCurrentX + px) * 16, 32 + (nCurrentY + py) * 16, 1*16, 0, 16, 16, skin[selectedSkin]);
                                break;
                            case 6:
                                DrawPartialImage(g, 16 + (nCurrentX + px) * 16, 32 + (nCurrentY + py) * 16, 2*16, 0, 16, 16, skin[selectedSkin]);
                                break;
                        }
                    }
                }
            }
            if(!vLines.isEmpty()) {
                for (int v:vLines) {
                    for (int px = 1; px < nFieldWidth-1; px++) {
                        for (int py = v; py > 0; py--) {
                            pField[py*nFieldWidth+px] = pField[(py-1)*nFieldWidth+px];
                        }
                        pField[px] = ' ';
                    }
                }
                vLines.clear();
            }
            DrawFormattedString(g,10,20,Color.WHITE,"SCORE : %10d",nScore);

            DrawString(g,300,15,Color.WHITE,"Stored :");
            DrawRect(g,300,25,(4*16),(4*16) + 32,Color.WHITE);
            //Draw Stored Piece
            for (int px = 0; px < 4; px++) {
                for (int py = 0; py < 4; py++) {
                    if (nStoredPiece != -1) {
                        if (tetrominos[nStoredPiece].charAt(Rotate(px, py, 0)) == 'X') {
                            switch (nStoredPiece) {
                                case 0:
                                    DrawPartialImage(g, 300 + px * 16, 32 + py * 16, 3 * 16, 0, 16, 16, skin[selectedSkin]);
                                    break;
                                case 1:
                                    DrawPartialImage(g, 300 + px * 16, 32 + py * 16, 6 * 16, 0, 16, 16, skin[selectedSkin]);
                                    break;
                                case 2:
                                    DrawPartialImage(g, 300 + px * 16, 32 + py * 16, 0 * 16, 0, 16, 16, skin[selectedSkin]);
                                    break;
                                case 3:
                                    DrawPartialImage(g, 300 + px * 16, 32 + py * 16, 4 * 16, 0, 16, 16, skin[selectedSkin]);
                                    break;
                                case 4:
                                    DrawPartialImage(g, 300 + px * 16, 32 + py * 16, 5 * 16, 0, 16, 16, skin[selectedSkin]);
                                    break;
                                case 5:
                                    DrawPartialImage(g, 300 + px * 16, 32 + py * 16, 1 * 16, 0, 16, 16, skin[selectedSkin]);
                                    break;
                                case 6:
                                    DrawPartialImage(g, 300 + px * 16, 32 + py * 16, 2 * 16, 0, 16, 16, skin[selectedSkin]);
                                    break;
                            }
                        }
                    }
                }
            }
            return true;
        }

        int Rotate(int px, int py, int r) {
            switch (r % 4) {
                case 0:
                    return py * 4 + px;              // 0 degrees
                case 1:
                    return 12 + py - (px * 4);       // 90 degrees
                case 2:
                    return 15 - (py * 4) - px;         // 180 degrees
                case 3:
                    return 3 - py + (px * 4);          // 270 degrees
            }
            return 0;
        }

        boolean DoesPieceFit(int nTetromino, int nRotation, int nPosX, int nPosY) {
            for (int px = 0; px < 4; px++) {
                for (int py = 0; py < 4; py++) {
                    //Index in Piece
                    int pi = Rotate(px, py, nRotation);
                    //Index in Field
                    int fi = (nPosY + py) * nFieldWidth + (nPosX + px);
                    if (nPosX + px >= 0 && nPosX + px < nFieldWidth) {
                        if (nPosY + py >= 0 && nPosY + py < nFieldHeight) {
                            if (tetrominos[nTetromino].charAt(pi) == 'X' && pField[fi] != ' ')
                                return false; // Fail on First Hit;
                        }
                    }
                }
            }return true;
        }
    }
    public static void main(String[] args) {
        Example ex = new Example();
        if (ex.Construct("Tetris ", 800, 700,1, 1)) {
            System.out.println("[!] Build Successfully");
        }

    }
}
