/** 
 * By using LX Studio, you agree to the terms of the LX Studio Software
 * License and Distribution Agreement, available at: http://lx.studio/license
 *
 * Please note that the LX license is not open-source. The license
 * allows for free, non-commercial use.
 *
 * HERON ARTS MAKES NO WARRANTY, EXPRESS, IMPLIED, STATUTORY, OR
 * OTHERWISE, AND SPECIFICALLY DISCLAIMS ANY WARRANTY OF
 * MERCHANTABILITY, NON-INFRINGEMENT, OR FITNESS FOR A PARTICULAR
 * PURPOSE, WITH RESPECT TO THE SOFTWARE.
 */
package org.tinic.duckpondbar;

import java.io.File;

import java.util.Collections;
import java.util.List;
import java.util.Arrays;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;

import heronarts.lx.output.LXDatagram;
import heronarts.lx.output.ArtNetDatagram;
import heronarts.lx.output.FadecandyOutput;
import heronarts.lx.output.LXDatagramOutput;
import heronarts.lx.output.OPCOutput;

public class DuckPondBarHeadless {

  public static LXModel buildModel() {
    return DuckPondBar.create();
  }

  public static int[] getIndices(List<LXPoint> points) {
    int[] indices = new int[points.size()];
    for (int i = 0; i < points.size(); i++) {
      indices[i] = points.get(i).index;
    }
    return indices;
  }

  public static void addDatagram(LXDatagramOutput output, int universe, int[] indices, String address) {
      try {
        int total = indices.length;
        int start = 0;
        while (total > 0) {
          int[] split = Arrays.copyOfRange(indices, start, start + Math.min(total, 170)); //<>//
          ArtNetDatagram datagram = new ArtNetDatagram(split);
          datagram.setAddress(address);
          datagram.setByteOrder(LXDatagram.ByteOrder.RGB);  
          datagram.setUniverseNumber(universe);
          datagram.setSequenceEnabled(true);
          output.addDatagram(datagram);
          total -= split.length;
          start += split.length;
          universe++;
        }
    } catch (Exception x) {
      x.printStackTrace();
    }
  }

  public static void addOutputs(LXModel model, LX lx) {
    try {
      LXDatagramOutput output = new LXDatagramOutput(lx);

      BarTop bar = ((DuckPondBar)model).barTop();

      // Port A
      addDatagram(output, 0, getIndices(bar.barFront().getPoints()), bar.ip);
      // Port B
      addDatagram(output, 4, getIndices(bar.barBack().getPoints()), bar.ip);
    
      List<Umbrella> umbrellas = ((DuckPondBar)model).umbrellas();
      for (int u = 0 ; u < umbrellas.size(); u++) {
        // Port A
        addDatagram(output, 0, getIndices(umbrellas.get(u).getPoints()), umbrellas.get(u).ip);
      }

      final double MAX_BRIGHTNESS = 0.75;
      output.brightness.setNormalized(MAX_BRIGHTNESS);

      // Add the datagram output to the LX engine
      lx.addOutput(output);
    } catch (Exception x) {
      x.printStackTrace();
    }
  }

  public static void main(String[] args) {
    try {
      LXModel model = buildModel();
      LX lx = new LX(model);

      addOutputs(model, lx);

      if (args.length > 0) {
        lx.openProject(new File(args[0]));
      }

      lx.engine.start();
    } catch (Exception x) {
      System.err.println(x.getLocalizedMessage());
    }
  }
};
