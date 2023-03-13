package com.vzome.experiments;

import java.io.FileWriter;
import java.io.IOException;

import com.vzome.core.algebra.AlgebraicField;
import com.vzome.core.algebra.AlgebraicNumber;
import com.vzome.core.algebra.PentagonField;
import com.vzome.core.construction.Color;

public class LogoTransition {

	public static void main(String[] args) {
		final int nSteps = 60;
		final int midpoint = nSteps / 2;
		for (int i = 0; i <= nSteps; i++) {
			String symmetrySystem = i < midpoint ? OCTAHEDRAL_SYMM : ICOSAHEDRAL_SYMM;
			String otherSymmetries = i >= midpoint ? OCTAHEDRAL_SYMM : ICOSAHEDRAL_SYMM;
			Boolean isParallel = i >= midpoint;
			Double distance = transition(59.6729850769043, 34.08575439453125, i, nSteps);
			Double far = transition(119.3459701538086, 68.17150115966797, i, nSteps);
			Double near = transition(0.14918246865272522, 0.08521436154842377, i, nSteps);
			Double width = transition(26.852842330932617, 15.33858585357666, i, nSteps);
			Double lookAtX = transition(0.0, 0.0, i, nSteps);
			Double lookAtY = transition(-3.427051067352295, 0.0, i, nSteps);
			Double lookAtZ = transition(5.5450849533081055, 0.0, i, nSteps);
			Double upDirectionX = transition(-0.8263358473777771, 0.30901700258255005, i, nSteps);
			Double upDirectionY = transition(0.31366482377052307, -0.5, i, nSteps);
			Double upDirectionZ = transition(0.4677429497241974, 0.80901700258255, i, nSteps);
			Double lookDirectionX = transition(0.39686280488967896, 0.0, i, nSteps);
			Double lookDirectionY = transition(-0.2649680972099304, 0.8506507873535156, i, nSteps);
			Double lookDirectionZ = transition(0.8788013458251953, 0.525731086730957, i, nSteps);
			String background = transitionColor("175,200,220", "0,0,0", i, nSteps);
			String c0 = transitionWebColor("#AF87FF", "#AF0000", i, nSteps);
			String c1 = transitionWebColor("#F0A000", "#F0A000", i, nSteps);
			String c2 = transitionWebColor("#647100", "#007695", i, nSteps);
			String c3 = transitionWebColor("#750032", "#AF87FF", i, nSteps);
			String c4 = transitionWebColor("#750032", "#008D36", i, nSteps);
			String c5 = transitionWebColor("#AF87FF", "#AF0000", i, nSteps);
			String vertices = transitionVertices(verticesFrom, verticesTo, i, nSteps);
			
			String text = TEMPLATE.replace("%SymmetrySystem%", symmetrySystem);
			text = text.replace("%OtherSymmetries%", otherSymmetries);
			text = text.replace("%parallel%", isParallel.toString());
			text = text.replace("%distance%", distance.toString());
			text = text.replace("%far%", far.toString());
			text = text.replace("%near%", near.toString());
			text = text.replace("%width%", width.toString());
			text = text.replace("%lookAtX%", lookAtX.toString());
			text = text.replace("%lookAtY%", lookAtY.toString());
			text = text.replace("%lookAtZ%", lookAtZ.toString());
			text = text.replace("%upDirectionX%", upDirectionX.toString());
			text = text.replace("%upDirectionY%", upDirectionY.toString());
			text = text.replace("%upDirectionZ%", upDirectionZ.toString());
			text = text.replace("%lookDirectionX%", lookDirectionX.toString());
			text = text.replace("%lookDirectionY%", lookDirectionY.toString());
			text = text.replace("%lookDirectionZ%", lookDirectionZ.toString());
			text = text.replace("%background%", background);
			text = text.replace("%c0%", c0);
			text = text.replace("%c1%", c1);
			text = text.replace("%c2%", c2);
			text = text.replace("%c3%", c3);
			text = text.replace("%c4%", c4);
			text = text.replace("%c5%", c5);
			text = text.replace("%vertices%", vertices);
			
			String fileSpec = String.format("C:\\Users\\DHall\\Documents\\GitHub\\vzome-sharing\\2023\\03\\13\\new-vzome-logo\\LogoTransition-%02d.vZome", i);
			// A try-with-resources block closes the resource even if an exception occurs
			try (FileWriter myWriter = new FileWriter(fileSpec)) {
				myWriter.write(text);
				myWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Done");
	}
	
	private static int transition(int from, int to, int step, int nSteps) {
		int span = to - from;
		int pos = span * step / nSteps;
		int result = from + pos;
		return result;
	}

	private static double transition(double from, double to, int step, int nSteps) {
		double span = to - from;
		double pos = span * step / nSteps;
		double result = from + pos;
		return result;
	}

	private static AlgebraicNumber transition(AlgebraicNumber from, AlgebraicNumber to, int step, int nSteps) {
		AlgebraicNumber span = to.minus(from);
		AlgebraicNumber pos = span.timesRational(step, nSteps);
		AlgebraicNumber result = from.plus(pos);
		return result;
	}

	private static String transitionVertices(int[][][] from, int[][][] to, int step, int nSteps) {
		StringBuilder buf = new StringBuilder();
		buf.append("[\n");
		for(int vertex = 0; vertex < from.length; vertex++) {
			buf.append("    [ ");
			int[][] vertexFrom = from[vertex];
			int[][] vertexTo = to[vertex];
			for(int c = 0; c < vertexFrom.length; c++) {
				buf.append("[ ");
				int[] cFrom = vertexFrom[c];
				int[] cTo = vertexTo[c];
				AlgebraicNumber nFrom = field.createAlgebraicNumberFromTD(cFrom);
				AlgebraicNumber nTo = field.createAlgebraicNumberFromTD(cTo);
				AlgebraicNumber nTransition = transition(nFrom, nTo, step, nSteps);
				int[] result =nTransition.toTrailingDivisor();
				for(int i = 0; i < result.length; i++) {
					buf.append(result[i]);
					if(i < result.length-1) {
						buf.append(", ");
					}
				}
				buf.append(" ]");
				if(c < vertexFrom.length-1) {
					buf.append(", ");
				}
			}
			buf.append(" ]");
			if(vertex < from.length-1) {
				buf.append(",");
			}
			buf.append("\n");
		}
		buf.append("  ]");
		return buf.toString();
	}

	private static String transitionColor(String from, String to, int step, int nSteps) {
		Color fromColor = Color.parseColor(from);
		Color toColor = Color.parseColor(to);
		int r = transition(fromColor.getRed(), toColor.getRed(), step, nSteps);
		int g = transition(fromColor.getGreen(), toColor.getGreen(), step, nSteps);
		int b = transition(fromColor.getBlue(), toColor.getBlue(), step, nSteps);
		return r + "," + g + "," + b;
	}

	private static String transitionWebColor(String from, String to, int step, int nSteps) {
		Color fromColor = Color.parseWebColor(from);
		Color toColor = Color.parseWebColor(to);
		int r = transition(fromColor.getRed(), toColor.getRed(), step, nSteps);
		int g = transition(fromColor.getGreen(), toColor.getGreen(), step, nSteps);
		int b = transition(fromColor.getBlue(), toColor.getBlue(), step, nSteps);
		Color result = new Color(r,g,b);
		return result.toWebString();
	}
	
	private static final AlgebraicField field = new PentagonField();
	
	private static final int[][][] verticesFrom = new int[][][] 
		{
			{ {1, 1, 1 }, {-1, -1, 1 }, {1, 1, 1 } },
			{ {0, 0, 1 }, {0, 0, 1 }, {2, 4, 1 } },
			{ {1, 1, 1 }, {-1, -1, 1 }, {1, 1, 1 } },
			{ {0, 1, 1 }, {-2, -3, 1 }, {2, 3, 1 } },
			{ {1, 1, 1 }, {-1, -1, 1 }, {1, 1, 1 } },
			{ {0, 0, 1 }, {0, 0, 1 }, {2, 4, 1 } },
			{ {0, 0, 1 }, {0, 0, 1 }, {2, 4, 1 } },
			{ {-1, -2, 1 }, {-1, -2, 1 }, {1, 2, 1 } }, 
			{ {0, 1, 1 }, {-2, -3, 1 }, {2, 3, 1 } },
			{ {-1, -2, 1 }, {-1, -2, 1 }, {1, 2, 1 } }, 
			{ {-1, -2, 1 }, {-1, -2, 1 }, {1, 2, 1 } }, 
			{ {0, 1, 1 }, {-2, -3, 1 }, {2, 3, 1 } }
		};
	
	private static final int[][][] verticesTo = new int[][][]
		{ 
			{ { -2, -6, 5 }, { 23, -36, 10 }, { -59, 23, 10 } }, 
			{ { -2, -6, 5 }, { 3, 4, 10 }, { 1, 3, 10 } }, 
			{ { -2, -1, 5 }, { 13, -26, 10 }, { -29, 23, 10 } }, 
			{ { -2, -1, 5 }, { 13, -6, 10 }, { -29, 3, 10 } }, 
			{ { -2, -1, 5 }, { -7, 14, 10 }, { 31, 3, 10 } }, 
			{ { -2, -1, 5 }, { -7, 34, 10 }, { 31, -17, 10 } }, 
			{ { 3, 4, 5 }, { 23, -46, 10 }, { -69, 33, 10 } }, 
			{ { 3, 4, 5 }, { 23, -46, 10 }, { -69, 33, 10 } }, 
			{ { 3, 4, 5 }, { 13, -26, 10 }, { -39, 23, 10 } }, 
			{ { 3, 4, 5 }, { 3, -6, 10 }, { -9, 13, 10 } }, 
			{ { 3, 4, 5 }, { 3, 14, 10 }, { 11, -7, 10 } },
			{ { -2, -6, 5 }, { 23, -36, 10 }, { -59, 23, 10 } }
		};
	
	private static final String TEMPLATE = """
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<vzome:vZome xmlns:vzome="http://xml.vzome.com/vZome/4.0.0/" buildNumber="4242" field="golden" version="7.1">
  <EditHistory editNumber="6" lastStickyEdit="-1">
    <ImportColoredMeshJson scale="1 0">{
  "field" : "golden",
  "vertices" : %vertices%,
  "balls" : [ {
    "vertex" : 0,
    "color" : "#FFFFFF"
  }, {
    "vertex" : 1,
    "color" : "#FFFFFF"
  }, {
    "vertex" : 2,
    "color" : "#FFFFFF"
  }, {
    "vertex" : 3,
    "color" : "#FFFFFF"
  }, {
    "vertex" : 4,
    "color" : "#FFFFFF"
  }, {
    "vertex" : 5,
    "color" : "#FFFFFF"
  }, {
    "vertex" : 6,
    "color" : "#FFFFFF"
  }, {
    "vertex" : 7,
    "color" : "#FFFFFF"
  }, {
    "vertex" : 8,
    "color" : "#FFFFFF"
  }, {
    "vertex" : 9,
    "color" : "#FFFFFF"
  }, {
    "vertex" : 10,
    "color" : "#FFFFFF"
  }, {
    "vertex" : 11,
    "color" : "#FFFFFF"
  } ],
  "struts" : [ {
    "vertices" : [ 0, 7 ],
    "color" : "%c0%"
  }, {
    "vertices" : [ 1, 10 ],
    "color" : "%c1%"
  }, {
    "vertices" : [ 2, 8 ],
    "color" : "%c2%"
  }, {
    "vertices" : [ 3, 9 ],
    "color" : "%c3%"
  }, {
    "vertices" : [ 4, 5 ],
    "color" : "%c4%"
  }, {
    "vertices" : [ 6, 11 ],
    "color" : "%c5%"
  } ],
  "panels" : [ ]
}
</ImportColoredMeshJson>
    <BeginBlock/>
    <DeselectAll/>
    <SelectManifestation point="0 0 0 0 0 0"/>
    <EndBlock/>
    <Delete/>
  </EditHistory>
  <notes/>
  <sceneModel ambientLight="41,41,41" background="%background%">
    <directionalLight color="235,235,228" x="1.0" y="-1.0" z="-1.0"/>
    <directionalLight color="228,228,235" x="-1.0" y="0.0" z="0.0"/>
    <directionalLight color="30,30,30" x="0.0" y="0.0" z="-1.0"/>
  </sceneModel>
  <Viewing>
    <ViewModel distance="%distance%" far="%far%" near="%near%" parallel="%parallel%" stereoAngle="0.0" width="%width%">
      <LookAtPoint x="%lookAtX%" y="%lookAtY%" z="%lookAtZ%"/>
      <UpDirection x="%upDirectionX%" y="%upDirectionY%" z="%upDirectionZ%"/>
      <LookDirection x="%lookDirectionX%" y="%lookDirectionY%" z="%lookDirectionZ%"/>
    </ViewModel>
  </Viewing>
%SymmetrySystem%  <OtherSymmetries>
  %OtherSymmetries%  </OtherSymmetries>
  <Tools/>
</vzome:vZome>
""";

	private static final String OCTAHEDRAL_SYMM = """
  <SymmetrySystem name="octahedral" renderingStyle="trapezoids">
    <Direction color="0,118,149" name="blue" orbit="[[0,0,1],[0,0,1]]"/>
    <Direction color="175,135,255" name="lavender" orbit="[[1,2,1],[-1,0,1]]"/>
    <Direction color="18,205,148" name="turquoise" orbit="[[-1,2,1],[1,-2,1]]"/>
    <Direction color="30,30,30" name="black" orbit="[[0,1,1],[1,-1,1]]"/>
    <Direction color="0,141,54" name="green" orbit="[[0,0,1],[-1,0,1]]"/>
    <Direction color="100,113,0" name="olive" orbit="[[1,0,1],[3,-2,1]]"/>
    <Direction color="117,0,50" name="maroon" orbit="[[-1,2,1],[1,0,1]]"/>
    <Direction color="107,53,26" name="brown" orbit="[[1,0,1],[-2,0,1]]"/>
    <Direction color="240,160,0" name="yellow" orbit="[[1,0,1],[-1,0,1]]"/>
    <Direction color="175,0,0" name="red" orbit="[[-1,1,1],[0,0,1]]"/>
    <Direction color="108,0,198" name="purple" orbit="[[0,0,1],[2,-1,1]]"/>
  </SymmetrySystem>		
""";

	private static final String ICOSAHEDRAL_SYMM = """
  <SymmetrySystem name="icosahedral" renderingStyle="printable">
    <Direction color="0,118,149" name="blue" orbit="[[0,0,1],[0,0,1]]"/>
    <Direction color="0,141,54" name="green" orbit="[[2,-1,1],[5,-3,1]]"/>
    <Direction color="154,117,74" name="sand" orbit="[[-8,5,1],[5,-3,1]]"/>
    <Direction color="18,73,48" name="spruce" orbit="[[-5,4,11],[-5,4,11]]"/>
    <Direction color="175,0,0" name="red" orbit="[[-1,1,1],[0,0,1]]"/>
    <Direction color="255,126,106" name="coral" orbit="[[-3,2,2],[-1,1,2]]"/>
    <Direction color="30,30,30" name="black" orbit="[[-2,3,11],[-7,5,11]]"/>
    <Direction color="117,0,50" name="maroon" orbit="[[5,-3,1],[0,0,1]]"/>
    <Direction color="230,245,62" name="sulfur" orbit="[[-1,1,3],[0,0,1]]"/>
    <Direction color="240,160,0" name="yellow" orbit="[[0,0,1],[2,-1,1]]"/>
    <Direction color="255,51,143" name="rose" orbit="[[0,0,1],[-4,3,5]]"/>
    <Direction color="18,205,148" name="turquoise" orbit="[[2,-1,2],[-3,2,2]]"/>
    <Direction color="116,195,0" name="apple" orbit="[[2,-1,3],[-1,1,3]]"/>
    <Direction color="136,37,0" name="cinnamon" orbit="[[5,-3,2],[2,-1,2]]"/>
    <Direction color="108,0,198" name="purple" orbit="[[2,-1,1],[0,0,1]]"/>
    <Direction color="220,76,0" name="orange" orbit="[[-4,3,5],[3,-1,5]]"/>
    <Direction color="0,0,153" name="navy" orbit="[[-1,1,2],[2,-1,2]]"/>
    <Direction color="107,53,26" name="brown" orbit="[[2,-1,3],[5,-3,3]]"/>
    <Direction color="175,135,255" name="lavender" orbit="[[-3,2,1],[-3,2,1]]"/>
    <Direction color="100,113,0" name="olive" orbit="[[3,-1,5],[0,0,1]]"/>
  </SymmetrySystem>
""";

}
