/*
 * Copyright (c) 2021-2023, Adel Noureddine, Université de Pau et des Pays de l'Adour.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the
 * GNU General Public License v3.0 only (GPL-3.0-only)
 * which accompanies this distribution, and is available at
 * https://www.gnu.org/licenses/gpl-3.0.en.html
 */

using System;
using System.ComponentModel;

namespace org_noureddine_joularjx_gui.Models
{
    /// <summary>
    /// A method that has been used into the application analysed by joularjx
    /// </summary>
    public class Method : INotifyPropertyChanged
    {
        private string _name;
        private double _powerConsumption;
        public double ConsumptionPercentage { get; set; }

        public string Name
        {
            get { return _name; }
            set
            {
                if (_name != value)
                {
                    _name = value;
                    OnPropertyChanged(nameof(Name));
                }
            }
        }

        public double PowerConsumption

        {
            get { return _powerConsumption; }
            set
            {
                if (_powerConsumption != value)
                {
                    _powerConsumption = value;
                    OnPropertyChanged(nameof(PowerConsumption));
                }
            }
        }

        public void UpdatePercentage(double totalPowerConsumption)
        {
            if(totalPowerConsumption == 0)
            {
                ConsumptionPercentage = 0;
            }
            else
            {
                ConsumptionPercentage = ((PowerConsumption * 100) / totalPowerConsumption);
                ConsumptionPercentage = Math.Round(ConsumptionPercentage, 2);
            }
        }

        public Method(string name, double totalPowerConsumption)
        {
            Name = name;
            PowerConsumption = totalPowerConsumption;
        }

        public event PropertyChangedEventHandler PropertyChanged;

        protected virtual void OnPropertyChanged(string propertyName)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }
    }
}
