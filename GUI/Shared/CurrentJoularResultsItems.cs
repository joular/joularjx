/*
 * Copyright (c) 2021-2023, Adel Noureddine, Université de Pau et des Pays de l'Adour.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the
 * GNU General Public License v3.0 only (GPL-3.0-only)
 * which accompanies this distribution, and is available at
 * https://www.gnu.org/licenses/gpl-3.0.en.html
 */

using org_noureddine_joularjx_gui.Models;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Text;
using System.Threading.Tasks;

namespace org_noureddine_joularjx_gui.Shared
{
    /// <summary>
    /// The current joular results items 
    /// </summary>
    public static class CurrentJoularResultsItems
    {
        public static string joularResultsFolderPath;
        public static List<PID> PIDs {  get; set; }
        public static PID PID { get; set; }
        public static List<Method> AppMethodsTotalConsumptions { get; set; }
        public static List<Method> AppCaltreesTotalConsumptions { get; set; }
        public static List<Method> AllMethodsTotalConsumptions { get; set; }
        public static List<Method> AllCaltreesTotalConsumptions { get; set; }
        public static List<MethodConsumptionPerTimestamp> MethodConsumptionEvolution { get; set; }        
    }
}
