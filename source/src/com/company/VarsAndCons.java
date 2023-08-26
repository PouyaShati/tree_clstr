package com.company;

import java.util.ArrayList;
import java.util.HashMap;


public class VarsAndCons
{

    public static int getVarA(int j, int t)
    {
        int result;

        result = t*Main.f + j + 1;

        if(Main.base_var_cnt < result)
            Main.base_var_cnt = result;

        return result;
    }

    public static int getVarS(int i, int t)
    {
        int result;

        result = Main.tree.branchCount * Main.f + t*Main.n + i + 1;

        if(Main.base_var_cnt < result)
            Main.base_var_cnt = result;

        return result;
    }

    public static int getVarZ(int i, int t)
    {
        int result = Main.tree.branchCount * Main.f + Main.tree.branchCount*Main.n + (t-Main.tree.branchCount) * Main.n + i + 1;

        if(Main.base_var_cnt < result)
            Main.base_var_cnt = result;

        return result;
    }

    public static int getVarG(int t, int g)
    {
        int result;

        result = Main.tree.branchCount * Main.f + Main.tree.branchCount*Main.n + Main.tree.leafCount * Main.n + (t-Main.tree.branchCount) * (Main.k-1) + g + 1;

        if(Main.base_var_cnt < result)
            Main.base_var_cnt = result;

        return result;
    }


    public static int getVarX(int i, int ind)
    {
        int result;

        if(Main.useTree)
            result = Main.tree.branchCount * Main.f + Main.tree.branchCount*Main.n + Main.tree.leafCount * Main.n + Main.tree.leafCount * (Main.k-1);
        else
            result = 0;

        result += i*(Main.k-1) + ind + 1;

        if(Main.base_var_cnt < result)
            Main.base_var_cnt = result;

        return result;
    }


    public static int getVarDC(int w)
    {
        int result;

        if(Main.useTree)
            result = Main.tree.branchCount * Main.f + Main.tree.branchCount*Main.n + Main.tree.leafCount * Main.n + Main.tree.leafCount * (Main.k-1) + Main.n * (Main.k-1) + w + 1;
        else
            result = Main.n * (Main.k-1) + w + 1;

        if(Main.base_var_cnt + Main.obj_var_cnt < result)
            Main.obj_var_cnt = result - Main.base_var_cnt;

        return result;
    }


    public static int getVarDM(int w, int groupCount)
    {
        int result;

        if(Main.useTree)
        {
            result = Main.tree.branchCount * Main.f + Main.tree.branchCount*Main.n + Main.tree.leafCount * Main.n + Main.tree.leafCount * (Main.k-1) + Main.n * (Main.k-1) + w + 1;
            if(!Main.objective.equals("bcm"))
                result += groupCount;
        }
        else
        {
            result = Main.n * (Main.k-1) + w + 1;
            if(!Main.objective.equals("bcm"))
                result += groupCount;
        }

        if(Main.base_var_cnt + Main.obj_var_cnt < result)
            Main.obj_var_cnt = result - Main.base_var_cnt;

        return result;
    }


    public static void addConForceClusters(int force)
    {
        int[] temp = new int[Main.n];
        for(int i=0; i<Main.n; i++)
            temp[i] = getVarX(i, force-2);
        Main.baseHardClauses.add(temp);
    }

    public static void addConX() // only used for flat clustering
    {
        for(int i=0; i<Main.n; i++)
            for(int ind=1; ind<Main.k-1; ind++)
                Main.baseHardClauses.add(new int[]{-getVarX(i, ind), getVarX(i, ind-1)});
    }

    public static void addXTieBreaker()
    {
        for(int i=0; i<Main.k-1; i++)
            Main.baseHardClauses.add(new int[]{-getVarX(i, i)});

        for(int i=1; i<Main.n; i++)
        for(int ind=1; ind<i && ind<Main.k-1; ind++)
        {
            int[] temp = new int[i+1];
            temp[0] = -getVarX(i,ind);

            for(int i2=0; i2<i; i2++)
                temp[i2+1] = getVarX(i2,ind-1);

            Main.baseHardClauses.add(temp);
        }
    }

    public static void addConA()
    {
        addConA(Main.tree);
    }

    public static void addConA(Node node)
    {
        if(node.left != null)
        {
            int[] temp = new int[Main.f];
            for (int j = 0; j < temp.length; j++)
                temp[j] = getVarA(j, node.id);
            Main.baseHardClauses.add(temp);

            for (int j1 = 0; j1 < temp.length; j1++)
                for (int j2 = j1 + 1; j2 < temp.length; j2++)
                    Main.baseHardClauses.add(new int[]{-getVarA(j1, node.id), -getVarA(j2, node.id)});

            addConA(node.left);
            addConA(node.right);
        }
    }


    public static void addConSplit()
    {
        ArrayList<Integer> seenNodes = new ArrayList<>();
        addConSplit(Main.tree, seenNodes);
    }

    public static void addConSplit(Node node, ArrayList<Integer> seenNodes)
    {
        if(node.left != null)
        {
            for(int j=0; j<Main.f; j++)
            {
                if(Main.mustSplit)
                {
                    Main.baseHardClauses.add(new int[]{-getVarA(j, node.id), getVarS(Main.sorted[0][j], node.id)});
                    if(!Main.categorical[j])
                        Main.baseHardClauses.add(new int[]{-getVarA(j, node.id), -getVarS(Main.sorted[Main.n-1][j], node.id)});
                }

                int pointer = 0;

                while(pointer < Main.n-1)
                {
                    if(Main.data[Main.sorted[pointer][j]][j] == Main.data[Main.sorted[pointer+1][j]][j])
                    {
                        Main.baseHardClauses.add(new int[]{-getVarA(j, node.id), -getVarS(Main.sorted[pointer][j], node.id), getVarS(Main.sorted[pointer + 1][j], node.id)});
                        Main.baseHardClauses.add(new int[]{-getVarA(j, node.id), getVarS(Main.sorted[pointer][j], node.id), -getVarS(Main.sorted[pointer + 1][j], node.id)});
                    }else
                    {
                        if(!Main.categorical[j])
                            Main.baseHardClauses.add(new int[]{-getVarA(j, node.id), getVarS(Main.sorted[pointer][j], node.id), -getVarS(Main.sorted[pointer + 1][j], node.id)});
                    }
                    pointer++;
                }
            }

            ArrayList<Integer> seenNodesLeft = new ArrayList<>();
            ArrayList<Integer> seenNodesRight = new ArrayList<>();

            for(int i=0; i<seenNodes.size(); i++)
            {
                seenNodesLeft.add(seenNodes.get(i));
                seenNodesRight.add(seenNodes.get(i));
            }

            seenNodesLeft.add(0);
            seenNodesRight.add(1);

            addConSplit(node.left, seenNodesLeft);
            addConSplit(node.right, seenNodesRight);
        }
    }


    public static void addConZ()
    {
        ArrayList<Integer> seenNodes = new ArrayList<>();
        addConZ(Main.tree, seenNodes);
    }


    public static void addConZ(Node node, ArrayList<Integer> seenNodes)
    {
        if(node.left != null)
        {
            ArrayList<Integer> seenNodesLeft = new ArrayList<>();
            ArrayList<Integer> seenNodesRight = new ArrayList<>();

            for(int i=0; i<seenNodes.size(); i++)
            {
                seenNodesLeft.add(seenNodes.get(i));
                seenNodesRight.add(seenNodes.get(i));
            }

            seenNodesLeft.add(0);
            seenNodesRight.add(1);

            addConZ(node.left, seenNodesLeft);
            addConZ(node.right, seenNodesRight);
        }else
        {
            for(int i=0; i<Main.n; i++)
            {
                ArrayList<Integer> temp1 = new ArrayList<>();
                temp1.add(getVarZ(i, node.id));

                Node tempNode = Main.tree;
                for(int k=0; k<seenNodes.size(); k++)
                {
                    if(seenNodes.get(k)  == 0)
                    {
                        temp1.add(-getVarS(i, tempNode.id));
                        Main.baseHardClauses.add(new int[]{-getVarZ(i, node.id), getVarS(i, tempNode.id)});

                        tempNode = tempNode.left;
                    }else
                    {
                        temp1.add(getVarS(i, tempNode.id));
                        Main.baseHardClauses.add(new int[]{-getVarZ(i, node.id), -getVarS(i, tempNode.id)});

                        tempNode = tempNode.right;
                    }

                }

                int[] temp1array = new int[temp1.size()];
                for(int c=0; c<temp1array.length; c++)
                    temp1array[c] = temp1.get(c);

                Main.baseHardClauses.add(temp1array);
            }
        }
    }

    public static void addConC(ArrayList<int[]> mustLinks, ArrayList<int[]> cannotLinks)
    {
        int[] pointsComponents = null;
        boolean[][] componentsDisconnected = null;

        pointsComponents = new int[Main.n];
        for(int i=0; i<Main.n; i++)
            pointsComponents[i] = i;

        for(int i=0; i<mustLinks.size(); i++)
        {
            int i1 = mustLinks.get(i)[0];
            int i2 = mustLinks.get(i)[1];

            if((Main.smartLinks) && pointsComponents[i1] == pointsComponents[i2])
                continue;

            for(int ind=0; ind < Main.k-1; ind++)
            {
                Main.objHardClauses.add(new int[]{getVarX(i1, ind), -getVarX(i2, ind)});
                Main.objHardClauses.add(new int[]{-getVarX(i1, ind), getVarX(i2, ind)});
            }

            for(int cnt=0; cnt<Main.n; cnt++)
                if(cnt != i1 && pointsComponents[cnt] == pointsComponents[i1])
                    pointsComponents[cnt] = pointsComponents[i2];
            pointsComponents[i1] = pointsComponents[i2];
        }

        componentsDisconnected = new boolean[Main.n][];
        for(int i=0; i<componentsDisconnected.length; i++)
            componentsDisconnected[i] = new boolean[Main.n];

        for(int i=0; i<cannotLinks.size(); i++)
        {
            int i1 = cannotLinks.get(i)[0];
            int i2 = cannotLinks.get(i)[1];

            if((Main.smartLinks) && componentsDisconnected[pointsComponents[i1]][pointsComponents[i2]])
                continue;

            if(pointsComponents[i1] == pointsComponents[i2])
                System.out.println("Infeasible!");

            Main.objHardClauses.add(new int[]{getVarX(i1, 0), getVarX(i2, 0)});
            Main.objHardClauses.add(new int[]{-getVarX(i1, Main.k-2), -getVarX(i2, Main.k-2)});

            for(int ind=0; ind < Main.k-2; ind++)
                Main.objHardClauses.add(new int[]{-getVarX(i1, ind), -getVarX(i2, ind), getVarX(i1, ind+1), getVarX(i2, ind+1)});

            if(Main.smartLinks)
            {
                componentsDisconnected[pointsComponents[i1]][pointsComponents[i2]] = true;
                componentsDisconnected[pointsComponents[i2]][pointsComponents[i1]] = true;
            }
        }


        if(Main.objective.equals("wcm") || Main.objective.equals("wcm+bcm_p"))
        {
            int[] pointsComponents_wcm = Util.copy(pointsComponents);
            boolean[][] componentsDisconnected_wcm = Util.copy(componentsDisconnected);

            int tp = Main.distanceThresholds.size()-1;

            for(int i=Main.distances.length-1; i>=0; i--)
                if(Main.distances[i] > 0)
                {
                    while(tp > 0 && Main.distances[i] < Main.distanceThresholds.get(tp))
                        tp--;

                    if(Main.groups[tp] < 0)
                        continue;

                    int i1 = Main.sortedI1[i];
                    int i2 = Main.sortedI2[i];

                    if((Main.smartLinks) && componentsDisconnected_wcm[pointsComponents_wcm[i1]][pointsComponents_wcm[i2]])
                        continue;

                    if(pointsComponents_wcm[i1] == pointsComponents_wcm[i2])
                    {
                        Main.objHardClauses.add(new int[]{getVarDC(Main.groups[tp])});
                        break;
                    }
                    else
                    {
                        if(Main.smartLinks)
                        {
                            componentsDisconnected_wcm[pointsComponents_wcm[i1]][pointsComponents_wcm[i2]] = true;
                            componentsDisconnected_wcm[pointsComponents_wcm[i2]][pointsComponents_wcm[i1]] = true;
                        }

                        Main.objHardClauses.add(new int[]{getVarDC(Main.groups[tp]), getVarX(i1, 0), getVarX(i2, 0)});
                        Main.objHardClauses.add(new int[]{getVarDC(Main.groups[tp]), -getVarX(i1, Main.k-2), -getVarX(i2, Main.k-2)});
                        for(int ind=0; ind < Main.k-2; ind++)
                            Main.objHardClauses.add(new int[]{getVarDC(Main.groups[tp]), -getVarX(i1, ind), -getVarX(i2, ind), getVarX(i1, ind+1), getVarX(i2, ind+1)});

                    }
                }
        }
        if(Main.objective.equals("bcm") || Main.objective.equals("wcm+bcm_p"))
        {
            int[] pointsComponents_bcm = Util.copy(pointsComponents);
            boolean[][] componentsDisconnected_bcm = Util.copy(componentsDisconnected);

            int tp = 0;

            for(int i=0; i<Main.distances.length; i++)
                if(Main.distances[i] > 0)
                {
                    while(tp+1 < Main.distanceThresholds.size() && Main.distances[i] >= Main.distanceThresholds.get(tp+1))
                        tp++;

                    if(Main.groups[tp] < 0)
                        continue;

                    int i1 = Main.sortedI1[i];
                    int i2 = Main.sortedI2[i];

                    if((Main.smartLinks) && pointsComponents_bcm[i1] == pointsComponents_bcm[i2])
                        continue;

                    if(componentsDisconnected_bcm[pointsComponents_bcm[i1]][pointsComponents_bcm[i2]])
                    {
                        Main.objHardClauses.add(new int[]{-getVarDM(Main.groups[tp], Main.groupCount)});
                        break;
                    }

                    for(int t=0; t<componentsDisconnected_bcm[pointsComponents_bcm[i2]].length; t++)
                        componentsDisconnected_bcm[pointsComponents_bcm[i2]][t] = componentsDisconnected_bcm[pointsComponents_bcm[i1]][t] || componentsDisconnected_bcm[pointsComponents_bcm[i2]][t];

                    for(int cnt=0; cnt<Main.n; cnt++)
                        if(cnt != i1 && pointsComponents_bcm[cnt] == pointsComponents_bcm[i1])
                            pointsComponents_bcm[cnt] = pointsComponents_bcm[i2];
                    pointsComponents_bcm[i1] = pointsComponents_bcm[i2];

                    for(int ind=0; ind < Main.k-1; ind++)
                    {
                        Main.objHardClauses.add(new int[]{-getVarDM(Main.groups[tp], Main.groupCount), getVarX(i1, ind), -getVarX(i2, ind)});
                        Main.objHardClauses.add(new int[]{-getVarDM(Main.groups[tp], Main.groupCount), -getVarX(i1, ind), getVarX(i2, ind)});
                    }

                }

        }
        if(Main.objective.equals("wcm+bcm_p"))
        {
            for(int c=0; c<Main.groupCount; c++)
                Main.objHardClauses.add(new int[]{-getVarDM(c, Main.groupCount), getVarDC(c)});
        }

        addObjC();
    }


    public static void addObjC()
    {
        if(Main.objective.equals("wcm") || Main.objective.equals("wcm+bcm_p"))
        {
            double lastWeight = 0.0;
            int tp = 0;

            for(int c=0; c<Main.groupCount; c++)
            {
                while(Main.groups[tp] != c)
                    tp++;

                if(c > 0)
                    Main.objHardClauses.add(new int[]{-getVarDC(c), getVarDC(c-1)});

                Main.objSoftClauses.add(new int[]{-getVarDC(c)});

                double weight;

                weight = Main.distanceThresholds.get(tp);

                if(Main.objective.equals("wcm") || Main.objective.equals("wcm+bcm_p"))
                {
                    Main.objSoftClausesWeights.add(1.0);
                }
                else
                {
                    Main.objSoftClausesWeights.add(weight - lastWeight);
                }

                lastWeight = weight;
            }
        }
        if(Main.objective.equals("bcm") || Main.objective.equals("wcm+bcm_p"))
        {
            double lastWeight = Main.distanceThresholds.get(Main.distanceThresholds.size()-1) + Main.distanceClassRange + 0.001;

            int tp = Main.groups.length-1;

            while(Main.groups[tp] < 0)
                tp--;

            for(int c=Main.groupCount-1; c>=0; c--)
            {
                while(tp > 0 && Main.groups[tp-1] >= c)
                    tp--;

                if(c > 0)
                    Main.objHardClauses.add(new int[]{-getVarDM(c, Main.groupCount), getVarDM(c - 1, Main.groupCount)});

                Main.objSoftClauses.add(new int[]{getVarDM(c, Main.groupCount)});

                double weight = Main.distanceThresholds.get(tp);

                if(Main.objective.equals("bcm") || Main.objective.equals("wcm+bcm_p"))
                {
                    Main.objSoftClausesWeights.add(1.0);
                }
                else
                {
                    Main.objSoftClausesWeights.add(lastWeight - weight);
                }

                lastWeight = weight;
            }
        }

    }


    public static void addConGP()
    {
        addConGP(Main.tree);
    }

    public static void addConGP(Node node)
    {
        if(node.left != null)
        {
            addConGP(node.left);
            addConGP(node.right);
        }else
        {
            for (int g1 = 0; g1 < Main.k-2; g1++)
                Main.baseHardClauses.add(new int[]{getVarG(node.id, g1), -getVarG(node.id, g1+1)});

            for(int i=0; i<Main.n; i++)
                for (int g1 = 0; g1 < Main.k-1; g1++)
                {
                    Main.baseHardClauses.add(new int[]{-getVarZ(i, node.id), -getVarG(node.id, g1), getVarX(i, g1)});
                    Main.baseHardClauses.add(new int[]{-getVarZ(i, node.id), getVarG(node.id, g1), -getVarX(i, g1)});
                }
        }
    }
}
