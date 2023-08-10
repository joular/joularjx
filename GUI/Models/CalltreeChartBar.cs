/*
 * Copyright (c) 2021-2023, Adel Noureddine, Université de Pau et des Pays de l'Adour.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the
 * GNU General Public License v3.0 only (GPL-3.0-only)
 * which accompanies this distribution, and is available at
 * https://www.gnu.org/licenses/gpl-3.0.en.html
 */

using Microsoft.UI;
using Microsoft.UI.Xaml.Media;

namespace org_noureddine_joularjx_gui.Models
{
    public class CalltreeChartBar
    {
        public SolidColorBrush RectangleColor { get; set; }
        public bool IsLastItem { get; set; }
        public string Text { get; set; }
        public int Length { get; set; }
        public static int MaxLength = 0;
        public CalltreeChartBar(string text, int maxLength = 0)
        {             
            Text = text;
            if (maxLength > 0)
            {
                MaxLength = maxLength;
            }
            Length = MaxLength;
            IsLastItem = false;
            UpdateRectangleColor();

        }
        public void UpdateRectangleColor()
        {
            if (IsLastItem)
            {
                RectangleColor = new SolidColorBrush(Colors.LimeGreen);
            }
            else
            {
                RectangleColor = new SolidColorBrush(Colors.SteelBlue);
            }
        }
    }
}
