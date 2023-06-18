/*
 * Copyright (c) 2021-2023, Adel Noureddine, Université de Pau et des Pays de l'Adour.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the
 * GNU General Public License v3.0 only (GPL-3.0-only)
 * which accompanies this distribution, and is available at
 * https://www.gnu.org/licenses/gpl-3.0.en.html
 */

using org_noureddine_joularjx_gui.Models;
using org_noureddine_joularjx_gui.Shared;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Navigation;
using Newtonsoft.Json;
using OxyPlot;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Linq;
using System.Reflection;
using System.Runtime.CompilerServices;


namespace org_noureddine_joularjx_gui.Pages
{
    /// <summary>
    /// The method page that containns the method consumption evolution (for the methods) or the calltree (for the calltrees)
    /// </summary>
    public sealed partial class MethodPage : Page, INotifyPropertyChanged
    {
        public event PropertyChangedEventHandler PropertyChanged;
        private MethodType.Type _methodType;
        public ObservableCollection<Method> Methods { get; set; }
        public ObservableCollection<CalltreeChartBar> CalltreeBarItems;
        public MethodConsumption MethodConsumption;
        private double _totalPowerConsumption;
        private PlotModel MethodConsumptionEvolutionChartModel;
        public double TotalPowerConsumption
        {
            get { return _totalPowerConsumption; }
            set
            {
                if (_totalPowerConsumption != value)
                {
                    _totalPowerConsumption = value;
                    OnPropertyChanged(nameof(TotalPowerConsumption));
                    NotifyPropertyChanged();
                }
            }
        }

        public MethodPage()
        {
            this.InitializeComponent();
            pIDTextBlock.Text = $"PID {CurrentJoularResultsItems.PID.Name}";
            this.DataContext = this;
            this.Methods = new ObservableCollection<Method>();
        }

        /// <summary>
        /// Invoked when the page is loaded
        /// </summary>
        /// <param name="e"></param>
        protected override void OnNavigatedTo(NavigationEventArgs e)
        {
            base.OnNavigatedTo(e);
            //Retrieves the page parameters and saves them
            if (e.NavigationMode == NavigationMode.New && e.Parameter is string navigationState)
            {
                MethodPageParameters parameters = JsonConvert.DeserializeObject<MethodPageParameters>(navigationState);
                _methodType = parameters.MethodType;
                _totalPowerConsumption = parameters.TotalPowerConsumption;
                Methods.Add(parameters.Method);
                this.DataContext = this;
            }
            //Customize the page according to the method type ("Method" or "Calltree")
            if(_methodType == MethodType.Type.Calltree)
            {
                methodPlotView.Visibility = Visibility.Collapsed;
                methodNameTextBlock.Text = "Calltree's branch (from the parent to the last child)";
                InitializeCalltreeChart();
            }
            else
            {
                calltreeChartScrollViewer.Visibility = Visibility.Collapsed;
                methodNameTextBlock.Text = "Method's Consumption Evolution";
                MethodConsumption = new MethodConsumption(CurrentJoularResultsItems.MethodConsumptionEvolution);
                MethodConsumptionEvolutionChartModel = new MethodsChartPlotModel(MethodConsumption).Model;
            }            
        }

        /// <summary>
        /// Fill "CalltreeBarItems" in order to have data to display
        /// </summary>
        private void InitializeCalltreeChart()
        {
            CalltreeBarItems ??= new ObservableCollection<CalltreeChartBar>();
            //Check if the methods list is empty
            if(Methods == null || Methods.Count == 0 ) { return; }

            string[] methodsInCalltree = Methods[0].Name.Split(';');
            //Retrieve  the methods names  save them into the "CalltreeBarItems"
            GetCalltreeChartBarWidth(methodsInCalltree);
            foreach (string method in methodsInCalltree)
            {           
                CalltreeBarItems.Add(new CalltreeChartBar(method));
            }
            CalltreeBarItems.Last().IsLastItem = true;
            CalltreeBarItems.Last().UpdateRectangleColor();

        }
        /// <summary>
        /// Calculate the necessary calltree's chart bar width in order to cover the largest method by size
        /// </summary>
        /// <param name="method"></param>
        /// <returns></returns>
        private void GetCalltreeChartBarWidth(string[] methodsInCalltree)
        {
            int maxMethodLength = 0;
            int characterWidth = 10;
            foreach(string method in methodsInCalltree)
            {
                if (method.Length > maxMethodLength)
                {
                    maxMethodLength = method.Length;
                }
            }
            CalltreeChartBar.MaxLength = maxMethodLength * characterWidth;
        }
        private void NotifyPropertyChanged([CallerMemberName] string propertyName = "")
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }
        private void OnPropertyChanged(string propertyName)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }
    }
}
