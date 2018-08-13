/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license. See LICENSE file in the project root for full license
 * information.
 */
package com.orange.holisticMonitoring.placement.constraint;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.ESat;

/**
 *
 * <p>
 * Project: PlacementCalculator.
 * @author Charles Prud'homme
 * @since 26/02/2018.
 */
public class PropXinSReif extends Propagator<Variable> {

    int var;
    SetVar set;
    BoolVar r;

    public PropXinSReif(int x, SetVar set, BoolVar r) {
        super(new Variable[]{set, r}, PropagatorPriority.BINARY, false);
        this.set = set;
        this.var = x;
        this.r = r;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (r.getLB() == 1) {
            setPassive();
            set.force(var, this);
        } else if (r.getUB() == 0) {
            if (set.remove(var, this) || !set.getUB().contains(var)) {
                setPassive();
            }
        } else {
            if (set.getLB().contains(var)) {
                setPassive();
                r.setToTrue(this);
            } else if (!set.getUB().contains(var)) {
                setPassive();
                r.setToFalse(this);
            }
        }
    }

    @Override
    public ESat isEntailed() {
        if(isCompletelyInstantiated()){
            if(r.isInstantiatedTo(1)){
                return ESat.eval(set.getLB().contains(var));
            }else{
                return ESat.eval(!set.getUB().contains(var));
            }
        }
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        return "(" + var +" ∈ " + set + ") <=> "+r.getName();
    }

}
