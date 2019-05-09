//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.alienchain.ui.ninepatch4j;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NinePatchChunk implements Serializable {
    private static final long serialVersionUID = -7353439224505296217L;
    private static final int[] sPaddingRect = new int[4];
    private boolean mVerticalStartWithPatch;
    private boolean mHorizontalStartWithPatch;
    private List<Rectangle> mFixed;
    private List<Rectangle> mPatches;
    private List<Rectangle> mHorizontalPatches;
    private List<Rectangle> mVerticalPatches;
    private NinePatchChunk.Pair<Integer> mHorizontalPadding;
    private NinePatchChunk.Pair<Integer> mVerticalPadding;

    public NinePatchChunk() {
    }

    public static NinePatchChunk create(BufferedImage image) {
        NinePatchChunk chunk = new NinePatchChunk();
        chunk.findPatches(image);
        return chunk;
    }

    public void draw(BufferedImage image, Graphics2D graphics2D, int x, int y, int scaledWidth, int scaledHeight, int destDensity, int srcDensity) {
        boolean scaling = destDensity != srcDensity && destDensity != 0 && srcDensity != 0;
        if(scaling) {
            try {
                graphics2D = (Graphics2D)graphics2D.create();
                float densityScale = (float)destDensity / (float)srcDensity;
                graphics2D.translate(x, y);
                graphics2D.scale((double)densityScale, (double)densityScale);
                scaledWidth = (int)((float)scaledWidth / densityScale);
                scaledHeight = (int)((float)scaledHeight / densityScale);
                 y = 0;
                 x = 0;
                this.draw(image, graphics2D, x, y, scaledWidth, scaledHeight);
            } finally {
                graphics2D.dispose();
            }
        } else {
            this.draw(image, graphics2D, x, y, scaledWidth, scaledHeight);
        }

    }

    private void draw(BufferedImage image, Graphics2D graphics2D, int x, int y, int scaledWidth, int scaledHeight) {
        if(scaledWidth > 1 && scaledHeight > 1) {
            Graphics2D g = (Graphics2D)graphics2D.create();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            try {
                if(this.mPatches.size() == 0) {
                    g.drawImage(image, x, y, scaledWidth, scaledHeight, (ImageObserver)null);
                    return;
                }

                g.translate(x, y);
                y = 0;
                x = 0;
                NinePatchChunk.DrawingData data = this.computePatches(scaledWidth, scaledHeight);
                int fixedIndex = 0;
                int horizontalIndex = 0;
                int verticalIndex = 0;
                int patchIndex = 0;
                float vWeightSum = 1.0F;
                float vRemainder = (float)data.mRemainderVertical;

                for(boolean vStretch = this.mVerticalStartWithPatch; y < scaledHeight - 1; vStretch = !vStretch) {
                    boolean hStretch = this.mHorizontalStartWithPatch;
                    int height = 0;
                    float vExtra = 0.0F;
                    float hWeightSum = 1.0F;

                    for(float hRemainder = (float)data.mRemainderHorizontal; x < scaledWidth - 1; hStretch = !hStretch) {
                        Rectangle r;
                        float extra;
                        int width;
                        if(!vStretch) {
                            if(hStretch) {
                                r = (Rectangle)this.mHorizontalPatches.get(horizontalIndex++);
                                extra = (float)r.width / data.mHorizontalPatchesSum;
                                width = (int)(extra * hRemainder / hWeightSum);
                                hWeightSum -= extra;
                                hRemainder -= (float)width;
                                g.drawImage(image, x, y, x + width, y + r.height, r.x, r.y, r.x + r.width, r.y + r.height, (ImageObserver)null);
                                x += width;
                            } else {
                                r = (Rectangle)this.mFixed.get(fixedIndex++);
                                g.drawImage(image, x, y, x + r.width, y + r.height, r.x, r.y, r.x + r.width, r.y + r.height, (ImageObserver)null);
                                x += r.width;
                            }

                            height = r.height;
                        } else if(hStretch) {
                            r = (Rectangle)this.mPatches.get(patchIndex++);
                            vExtra = (float)r.height / data.mVerticalPatchesSum;
                            height = (int)(vExtra * vRemainder / vWeightSum);
                            extra = (float)r.width / data.mHorizontalPatchesSum;
                            width = (int)(extra * hRemainder / hWeightSum);
                            hWeightSum -= extra;
                            hRemainder -= (float)width;
                            g.drawImage(image, x, y, x + width, y + height, r.x, r.y, r.x + r.width, r.y + r.height, (ImageObserver)null);
                            x += width;
                        } else {
                            r = (Rectangle)this.mVerticalPatches.get(verticalIndex++);
                            vExtra = (float)r.height / data.mVerticalPatchesSum;
                            height = (int)(vExtra * vRemainder / vWeightSum);
                            g.drawImage(image, x, y, x + r.width, y + height, r.x, r.y, r.x + r.width, r.y + r.height, (ImageObserver)null);
                            x += r.width;
                        }
                    }

                    x = 0;
                    y += height;
                    if(vStretch) {
                        vWeightSum -= vExtra;
                        vRemainder -= (float)height;
                    }
                }
            } finally {
                g.dispose();
            }

        }
    }

    public void getPadding(int[] padding) {
        padding[0] = ((Integer)this.mHorizontalPadding.mFirst).intValue();
        padding[2] = ((Integer)this.mHorizontalPadding.mSecond).intValue();
        padding[1] = ((Integer)this.mVerticalPadding.mFirst).intValue();
        padding[3] = ((Integer)this.mVerticalPadding.mSecond).intValue();
    }

    public int[] getPadding() {
        this.getPadding(sPaddingRect);
        return sPaddingRect;
    }

    private NinePatchChunk.DrawingData computePatches(int scaledWidth, int scaledHeight) {
        NinePatchChunk.DrawingData data = new NinePatchChunk.DrawingData();
        boolean measuredWidth = false;
        boolean endRow = true;
        int remainderHorizontal = 0;
        int remainderVertical = 0;
        int start;
        Rectangle rect;
        Iterator var10;
        if(this.mFixed.size() > 0) {
            start = ((Rectangle)this.mFixed.get(0)).y;
            var10 = this.mFixed.iterator();

            while(var10.hasNext()) {
                rect = (Rectangle)var10.next();
                if(rect.y > start) {
                    endRow = true;
                    measuredWidth = true;
                }

                if(!measuredWidth) {
                    remainderHorizontal += rect.width;
                }

                if(endRow) {
                    remainderVertical += rect.height;
                    endRow = false;
                    start = rect.y;
                }
            }
        }

        data.mRemainderHorizontal = scaledWidth - remainderHorizontal;
        data.mRemainderVertical = scaledHeight - remainderVertical;
        data.mHorizontalPatchesSum = 0.0F;
        if(this.mHorizontalPatches.size() > 0) {
            start = -1;
            var10 = this.mHorizontalPatches.iterator();

            while(var10.hasNext()) {
                rect = (Rectangle)var10.next();
                if(rect.x > start) {
                    data.mHorizontalPatchesSum = data.mHorizontalPatchesSum + (float)rect.width;
                    start = rect.x;
                }
            }
        } else {
            start = -1;
            var10 = this.mPatches.iterator();

            while(var10.hasNext()) {
                rect = (Rectangle)var10.next();
                if(rect.x > start) {
                    data.mHorizontalPatchesSum = data.mHorizontalPatchesSum + (float)rect.width;
                    start = rect.x;
                }
            }
        }

        data.mVerticalPatchesSum = 0.0F;
        if(this.mVerticalPatches.size() > 0) {
            start = -1;
            var10 = this.mVerticalPatches.iterator();

            while(var10.hasNext()) {
                rect = (Rectangle)var10.next();
                if(rect.y > start) {
                    data.mVerticalPatchesSum = data.mVerticalPatchesSum + (float)rect.height;
                    start = rect.y;
                }
            }
        } else {
            start = -1;
            var10 = this.mPatches.iterator();

            while(var10.hasNext()) {
                rect = (Rectangle)var10.next();
                if(rect.y > start) {
                    data.mVerticalPatchesSum = data.mVerticalPatchesSum + (float)rect.height;
                    start = rect.y;
                }
            }
        }

        return data;
    }

    private void findPatches(BufferedImage image) {
        int width = image.getWidth() - 2;
        int height = image.getHeight() - 2;
        int[] row = (int[])null;
        int[] column = (int[])null;
        row = GraphicsUtilities.getPixels(image, 1, 0, width, 1, row);
        column = GraphicsUtilities.getPixels(image, 0, 1, 1, height, column);
        boolean[] result = new boolean[1];
        NinePatchChunk.Pair<List<NinePatchChunk.Pair<Integer>>> left = this.getPatches(column, result);
        this.mVerticalStartWithPatch = result[0];
        result = new boolean[1];
        NinePatchChunk.Pair<List<NinePatchChunk.Pair<Integer>>> top = this.getPatches(row, result);
        this.mHorizontalStartWithPatch = result[0];
        this.mFixed = this.getRectangles((List)left.mFirst, (List)top.mFirst);
        this.mPatches = this.getRectangles((List)left.mSecond, (List)top.mSecond);
        if(this.mFixed.size() > 0) {
            this.mHorizontalPatches = this.getRectangles((List)left.mFirst, (List)top.mSecond);
            this.mVerticalPatches = this.getRectangles((List)left.mSecond, (List)top.mFirst);
        } else if(((List)top.mFirst).size() > 0) {
            this.mHorizontalPatches = new ArrayList(0);
            this.mVerticalPatches = this.getVerticalRectangles(height, (List)top.mFirst);
        } else if(((List)left.mFirst).size() > 0) {
            this.mHorizontalPatches = this.getHorizontalRectangles(width, (List)left.mFirst);
            this.mVerticalPatches = new ArrayList(0);
        } else {
            this.mHorizontalPatches = this.mVerticalPatches = new ArrayList(0);
        }

        row = GraphicsUtilities.getPixels(image, 1, height + 1, width, 1, row);
        column = GraphicsUtilities.getPixels(image, width + 1, 1, 1, height, column);
        top = this.getPatches(row, result);
        this.mHorizontalPadding = this.getPadding((List)top.mFirst);
        left = this.getPatches(column, result);
        this.mVerticalPadding = this.getPadding((List)left.mFirst);
    }

    private List<Rectangle> getVerticalRectangles(int imageHeight, List<NinePatchChunk.Pair<Integer>> topPairs) {
        List<Rectangle> rectangles = new ArrayList();
        Iterator var5 = topPairs.iterator();

        while(var5.hasNext()) {
            NinePatchChunk.Pair<Integer> top = (NinePatchChunk.Pair)var5.next();
            int x = ((Integer)top.mFirst).intValue();
            int width = ((Integer)top.mSecond).intValue() - ((Integer)top.mFirst).intValue();
            rectangles.add(new Rectangle(x, 0, width, imageHeight));
        }

        return rectangles;
    }

    private List<Rectangle> getHorizontalRectangles(int imageWidth, List<NinePatchChunk.Pair<Integer>> leftPairs) {
        List<Rectangle> rectangles = new ArrayList();
        Iterator var5 = leftPairs.iterator();

        while(var5.hasNext()) {
            NinePatchChunk.Pair<Integer> left = (NinePatchChunk.Pair)var5.next();
            int y = ((Integer)left.mFirst).intValue();
            int height = ((Integer)left.mSecond).intValue() - ((Integer)left.mFirst).intValue();
            rectangles.add(new Rectangle(0, y, imageWidth, height));
        }

        return rectangles;
    }

    private NinePatchChunk.Pair<Integer> getPadding(List<NinePatchChunk.Pair<Integer>> pairs) {
        if(pairs.size() == 0) {
            return new NinePatchChunk.Pair(Integer.valueOf(0), Integer.valueOf(0));
        } else if(pairs.size() == 1) {
            return ((Integer)((NinePatchChunk.Pair)pairs.get(0)).mFirst).intValue() == 0?new NinePatchChunk.Pair(Integer.valueOf(((Integer)((NinePatchChunk.Pair)pairs.get(0)).mSecond).intValue() - ((Integer)((NinePatchChunk.Pair)pairs.get(0)).mFirst).intValue()), Integer.valueOf(0)):new NinePatchChunk.Pair(Integer.valueOf(0), Integer.valueOf(((Integer)((NinePatchChunk.Pair)pairs.get(0)).mSecond).intValue() - ((Integer)((NinePatchChunk.Pair)pairs.get(0)).mFirst).intValue()));
        } else {
            int index = pairs.size() - 1;
            return new NinePatchChunk.Pair(Integer.valueOf(((Integer)((NinePatchChunk.Pair)pairs.get(0)).mSecond).intValue() - ((Integer)((NinePatchChunk.Pair)pairs.get(0)).mFirst).intValue()), Integer.valueOf(((Integer)((NinePatchChunk.Pair)pairs.get(index)).mSecond).intValue() - ((Integer)((NinePatchChunk.Pair)pairs.get(index)).mFirst).intValue()));
        }
    }

    private List<Rectangle> getRectangles(List<NinePatchChunk.Pair<Integer>> leftPairs, List<NinePatchChunk.Pair<Integer>> topPairs) {
        List<Rectangle> rectangles = new ArrayList();
        Iterator var5 = leftPairs.iterator();

        while(var5.hasNext()) {
            NinePatchChunk.Pair<Integer> left = (NinePatchChunk.Pair)var5.next();
            int y = ((Integer)left.mFirst).intValue();
            int height = ((Integer)left.mSecond).intValue() - ((Integer)left.mFirst).intValue();
            Iterator var9 = topPairs.iterator();

            while(var9.hasNext()) {
                NinePatchChunk.Pair<Integer> top = (NinePatchChunk.Pair)var9.next();
                int x = ((Integer)top.mFirst).intValue();
                int width = ((Integer)top.mSecond).intValue() - ((Integer)top.mFirst).intValue();
                rectangles.add(new Rectangle(x, y, width, height));
            }
        }

        return rectangles;
    }

    private NinePatchChunk.Pair<List<NinePatchChunk.Pair<Integer>>> getPatches(int[] pixels, boolean[] startWithPatch) {
        int lastIndex = 0;
        int lastPixel = pixels[0];
        boolean first = true;
        List<NinePatchChunk.Pair<Integer>> fixed = new ArrayList();
        List<NinePatchChunk.Pair<Integer>> patches = new ArrayList();

        for(int i = 0; i < pixels.length; ++i) {
            int pixel = pixels[i];
            if(pixel != lastPixel) {
                if(lastPixel == -16777216) {
                    if(first) {
                        startWithPatch[0] = true;
                    }

                    patches.add(new NinePatchChunk.Pair(Integer.valueOf(lastIndex), Integer.valueOf(i)));
                } else {
                    fixed.add(new NinePatchChunk.Pair(Integer.valueOf(lastIndex), Integer.valueOf(i)));
                }

                first = false;
                lastIndex = i;
                lastPixel = pixel;
            }
        }

        if(lastPixel == -16777216) {
            if(first) {
                startWithPatch[0] = true;
            }

            patches.add(new NinePatchChunk.Pair(Integer.valueOf(lastIndex), Integer.valueOf(pixels.length)));
        } else {
            fixed.add(new NinePatchChunk.Pair(Integer.valueOf(lastIndex), Integer.valueOf(pixels.length)));
        }

        if(patches.size() == 0) {
            patches.add(new NinePatchChunk.Pair(Integer.valueOf(1), Integer.valueOf(pixels.length)));
            startWithPatch[0] = true;
            fixed.clear();
        }

        return new NinePatchChunk.Pair(fixed, patches);
    }

    static final class DrawingData {
        private int mRemainderHorizontal;
        private int mRemainderVertical;
        private float mHorizontalPatchesSum;
        private float mVerticalPatchesSum;

        DrawingData() {
        }
    }

    static class Pair<E> implements Serializable {
        private static final long serialVersionUID = -2204108979541762418L;
        E mFirst;
        E mSecond;

        Pair(E first, E second) {
            this.mFirst = first;
            this.mSecond = second;
        }

        public String toString() {
            return "Pair[" + this.mFirst + ", " + this.mSecond + "]";
        }
    }
}
