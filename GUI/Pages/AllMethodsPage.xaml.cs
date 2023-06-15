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
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Navigation;
using Newtonsoft.Json;
using System;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Linq;
using System.Runtime.CompilerServices;


namespace org_noureddine_joularjx_gui.Pages
{
    /// <summary>
    /// The page that shows methods according to the application type ("All" or "App") and the method type ("Method" or "Calltree")
    /// </summary>
    public sealed partial class AllMethodsPage : Page, INotifyPropertyChanged
    {
        public event PropertyChangedEventHandler PropertyChanged;
        private AppType.Type _appType;
        private MethodType.Type _methodType;
        public ObservableCollection<Method> Methods { get; set; }
        private double _totalPowerConsumption;
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
        private ObservableCollection<Method> filteredMethods;
        /// <summary>
        /// The methods displayed according to the results from the search bar
        /// </summary>
        public ObservableCollection<Method> FilteredMethods
        {
            get { return filteredMethods; }
            set
            {
                filteredMethods = value;
                OnPropertyChanged(nameof(FilteredMethods));
            }
        }

        public AllMethodsPage()
        {
            this.InitializeComponent();
            pIDTextBlock.Text = $"PID {CurrentJoularResultsItems.PID.Name}";
            this.DataContext = this;
        }

        /// <summary>
        /// Update the methods number founded trough the search bar and update the convenient textblock
        /// </summary>
        private void UpdateMethodsNumberFoundTextblock()
        {
            string number = "0";
            if(FilteredMethods != null && FilteredMethods.Any())
            {
                number = $"{FilteredMethods.Count()}";
            }
            methodsNumberFoundTextBlock.Text = $"{number} method(s) found";
        }
        /// <summary>
        /// Invoked when the page is loaded. Retrieves the page's parameters :
            /// The type of the method (Method or Calltree) 
            /// and the type of the app (App or All)
        /// </summary>
        /// <param name="e"></param>
        protected override void OnNavigatedTo(NavigationEventArgs e)
        {
            base.OnNavigatedTo(e);
            //Retrieve the page's parameters and save them
            if (e.NavigationMode == NavigationMode.New && e.Parameter is string AllMethodsPageParametersToObject)
            {
                AllMethodsPageParameters parameters = JsonConvert.DeserializeObject<AllMethodsPageParameters>(AllMethodsPageParametersToObject);
                _methodType = parameters.MethodType;
                _appType = parameters.AppType;                
                this.DataContext = this;
            }
            UpdateTitleTextBlock();
            UpdateMethodsList();
            UpdateMethodsNumberFoundTextblock();
            TotalPowerConsumption = CalculateTotalPowerConsumption();
            RetrieveMethodsConsumptionsPercentages();
            //Don't show the methods list view if the methods list is empty
            if (!Methods.Any())
            {
                methodListView.Visibility = Microsoft.UI.Xaml.Visibility.Collapsed;
            }
        }
        /// <summary>
        /// Update the methods list according to the method type and the app type
        /// </summary>
        private void UpdateMethodsList()
        {
            if ((_methodType == MethodType.Type.Method) && (_appType == AppType.Type.App))
            {
                Methods = new ObservableCollection<Method>(
                    CurrentJoularResultsItems.AppMethodsTotalConsumptions);
            }
            else if ((_methodType == MethodType.Type.Method) && (_appType == AppType.Type.All))
            {
                Methods = new ObservableCollection<Method>(
                    CurrentJoularResultsItems.AllMethodsTotalConsumptions);
            }
            else if ((_methodType == MethodType.Type.Calltree) && (_appType == AppType.Type.App))
            {
                Methods = new ObservableCollection<Method>(
                    CurrentJoularResultsItems.AppCaltreesTotalConsumptions);
            }
            else if ((_methodType == MethodType.Type.Calltree) && (_appType == AppType.Type.All))
            {
                Methods = new ObservableCollection<Method>(
                    CurrentJoularResultsItems.AllCaltreesTotalConsumptions);
            }
            FilteredMethods = new ObservableCollection<Method>(Methods);
        }     
        private void OnPropertyChanged(string propertyName)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }
        /// <summary>
        /// Update the title textblock to display the convenient PID's name
        /// </summary>
        private void UpdateTitleTextBlock()
        {
            string pageTitleTextBlockContent = string.Empty;

            if(_appType == AppType.Type.App)
            {
                pageTitleTextBlockContent += "App ";
            }
            else
            {
                pageTitleTextBlockContent += "All ";
            }

            if(_methodType == MethodType.Type.Method)
            {
                pageTitleTextBlockContent += "Methods ";
            }
            else
            {
                pageTitleTextBlockContent += "Calltrees ";
            }

            pageTitleTextBlockContent += "Power Consumption";
            titleTextBlock.Text = pageTitleTextBlockContent;            
        }
        /// <summary>
        /// Invoked when a method has been clicked into the methods list view.
        /// Retrieve the method selected and navigates to the "MethodPage" page with its parameters
        /// 
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void MethodListView_ItemClick(object sender, ItemClickEventArgs e)
        {     
            Method selectedMethod = (Method)e.ClickedItem;
            //Get the method's power consumption evolution
            if(_methodType == MethodType.Type.Method)
            {
                CurrentJoularResultsItems.MethodConsumptionEvolution = AllPIDsPage.GetMethodConsumptionEvolution(selectedMethod, this._appType);
            }            
            Frame currentFrame = this.FindFirstParentOfType<Frame>();
            string methodPageParametersObject;
            MethodPageParameters methodPageParameters = new MethodPageParameters();
            methodPageParameters.Method = selectedMethod;
            methodPageParameters.MethodType = this._methodType;
            methodPageParameters.TotalPowerConsumption = this._totalPowerConsumption;
            methodPageParametersObject = JsonConvert.SerializeObject(methodPageParameters);
            currentFrame.Navigate(typeof(MethodPage), methodPageParametersObject);
        }

        private void NotifyPropertyChanged([CallerMemberName] string propertyName = "")
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }

        private void RetrieveMethodsConsumptionsPercentages()
        {
            foreach(Method methods in Methods)
            {
                methods.UpdatePercentage(TotalPowerConsumption);
            }
        }

        private double CalculateTotalPowerConsumption()
        {
            double totalPowerConsumption = 0;
            foreach (Method method in Methods)
            {
                totalPowerConsumption += method.PowerConsumption;
            }
            return totalPowerConsumption;
        }
        /// <summary>
        /// Invoked when the text into the method's search bar changes.
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="args"></param>
        private void AutoSuggestBox_TextChanged(AutoSuggestBox sender, AutoSuggestBoxTextChangedEventArgs args)
        {
            if (args.Reason == AutoSuggestionBoxTextChangeReason.UserInput)
            {
                string userInput = sender.Text.Trim();

                // The methods displayed are those that contains the text into the searchBar
                FilteredMethods = new ObservableCollection<Method>(
                    Methods.Where(m => m.Name.Contains(userInput, StringComparison.OrdinalIgnoreCase)));
            }
            UpdateMethodsNumberFoundTextblock();
        }
    }
}
