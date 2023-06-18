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
using OxyPlot.Axes;
using OxyPlot.Series;
using OxyPlot;

namespace org_noureddine_joularjx_gui.Models
{
    /// <summary>
    /// The plot model for a method's consumption evolution
    /// </summary>
    public class MethodsChartPlotModel
    {
        private static MethodConsumption _methodConsumption;
        public PlotModel Model = new();
        private readonly LineSeries _lineSeries = new()
        {
            Title = "Line Series",
            MarkerType = MarkerType.Circle,
            Color = OxyColor.FromRgb(0, 120, 215)
        };
        private readonly LinearAxis _xAxis = new()
        {
            Title="\nTimestamp (S)",
            Position = AxisPosition.Bottom,
            Angle = -45,

        };
        private readonly LinearAxis _yAxis = new()
        {
            Title = "Power Consumption (J)\n",
            Position = AxisPosition.Left,
        };
        public MethodsChartPlotModel(MethodConsumption methodConsumption)
        {
            _methodConsumption = methodConsumption;
            Model.Axes.Add(_xAxis);
            Model.Axes.Add(_yAxis);
            _lineSeries.Points.AddRange(GetDataPoints());
            //Model.PlotMargins = new OxyThickness(0);
            Model.Series.Add(_lineSeries);     
        }
        /// <summary>
        /// Retrieve datapoints from the "_methoddConsumption.Evolution" list
        /// </summary>
        /// <returns></returns>
        private static IEnumerable<DataPoint> GetDataPoints()
        {
            List<DataPoint> dataPoints = new();

            foreach (MethodConsumptionPerTimestamp consumptionPerTime in _methodConsumption.Evolution)
            {
                DataPoint dataPoint = new(consumptionPerTime.Timestamp, consumptionPerTime.Consumption);
                dataPoints.Add(dataPoint);
            }
            return dataPoints;
        }
    }
}
