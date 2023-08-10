/*
 * Copyright (c) 2021-2023, Adel Noureddine, Université de Pau et des Pays de l'Adour.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the
 * GNU General Public License v3.0 only (GPL-3.0-only)
 * which accompanies this distribution, and is available at
 * https://www.gnu.org/licenses/gpl-3.0.en.html
 */

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace org_noureddine_joularjx_gui.Models
{
    /// <summary>
    /// The method type ("Method" or "Calltree")
    /// </summary>
    public static class MethodType
    {
        public enum Type
        {
            Method,
            Calltree
        }
    }
}
