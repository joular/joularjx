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
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.IO;
using System.Linq;


namespace org_noureddine_joularjx_gui.Pages
{
    /// <summary>
    /// An empty page that can be used on its own or navigated to within a Frame.
    /// </summary>
    public sealed partial class AllPIDsPage : Page
    {
        private List<string> _allPIDs { get; set; }
        private string _searchText { get; set; }
        public static Button NavigateToAllMethodPageButton { get; set; }
        public ObservableCollection<PID> PIDs { get; set; }
        public ObservableCollection<PID> FilteredPIDs { get; set; }

        public AllPIDsPage()
        {
            this.InitializeComponent();
            NavigateToAllMethodPageButton = navigateToAllMethodsPageButton;
            this.DataContext = this;
        }
        /// <summary>
        /// Invoked when the page is loaded.
        /// Retrieve page's parameters and save them
        /// </summary>
        /// <param name="e"></param>
        protected override void OnNavigatedTo(NavigationEventArgs e)
        {
            base.OnNavigatedTo(e);
            if (e.NavigationMode == NavigationMode.New && e.Parameter is string AllPIDsPageParametersObject)
            {
                List<PID> parameters = JsonConvert.DeserializeObject<List<PID>>(AllPIDsPageParametersObject);
                PIDs = new ObservableCollection<PID>(parameters);
                FilteredPIDs = new ObservableCollection<PID>(PIDs);
                _allPIDs = PIDs.Select(p => p.Name).ToList();
                this.DataContext = this;
            }
        }
        /// <summary>
        /// Invoked when the text into the PID's search bar changes.
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="args"></param>
        private void AutoSuggestBox_TextChanged(AutoSuggestBox sender, AutoSuggestBoxTextChangedEventArgs args)
        {
            if (args.Reason == AutoSuggestionBoxTextChangeReason.UserInput)
            {
                string searchText = sender.Text.ToLower();
                List<PID> filteredPIDs = PIDs.Where(p => p.Name.ToLower().Contains(searchText)).ToList();
                FilteredPIDs = new ObservableCollection<PID>(filteredPIDs);
                PIDsListView.ItemsSource = FilteredPIDs;
            }     
        }
        /// <summary>
        /// Invoked when an item (a PID) is selected into the PIDs list view
        /// Retrieve the selected PID and navigate to "AllMethodsPage"
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void PIDListView_ItemClick(object sender, ItemClickEventArgs e)
        {
            PID pIDCardSelected = (PID)e.ClickedItem;
            CurrentJoularResultsItems.PID = new PID(pIDCardSelected.Name, pIDCardSelected.CreationDate);
            GetPIDMethodsTotalConsumptions();
            NavigationView navigationView = this.FindFirstParentOfType<NavigationView>();
            //Enable all navigation view items
            foreach (NavigationViewItemBase item in navigationView.MenuItems)
            {
                item.IsEnabled = true;
            }
            //Expand the "PIDPage" Navigation Item if the window is large enough
            if(navigationView.DisplayMode == NavigationViewDisplayMode.Expanded)
            {
                foreach (NavigationViewItemBase item in navigationView.MenuItems)
                {
                    if (item is NavigationViewItem navigationItem && navigationItem.Tag?.ToString() == "PIDPage")
                    {
                        navigationItem.IsExpanded = true;
                        break;
                    }
                }
            }
            // Selects the MethodsAppPage navigation view item to navigate towards the "AllMethodsPage" page
            navigationView.SelectedItem = navigationView.MenuItems.OfType<NavigationViewItem>()
                                                .FirstOrDefault(item => item.Tag.ToString() == "PIDPage")
                                                .MenuItems.OfType<NavigationViewItem>()
                                                .FirstOrDefault(item => item.Tag.ToString() == "MethodsPage")
                                                .MenuItems.OfType<NavigationViewItem>()
                                                .FirstOrDefault(item => item.Tag.ToString() == "MethodsAppPage");

        }
        /// <summary>
        /// Retrieve the methods total consumptions according to the methods type ("Method" or "Calltree") and the app type ("App" or "All").
        /// Save them into the currentJoularResults static clas
        /// </summary>
        public void GetPIDMethodsTotalConsumptions()
        {
            CurrentJoularResultsItems.AppMethodsTotalConsumptions = GetPIDMethods(AppType.Type.App, MethodType.Type.Method);
            CurrentJoularResultsItems.AppCaltreesTotalConsumptions = GetPIDMethods(AppType.Type.App, MethodType.Type.Calltree);
            CurrentJoularResultsItems.AllMethodsTotalConsumptions = GetPIDMethods(AppType.Type.All, MethodType.Type.Method);
            CurrentJoularResultsItems.AllCaltreesTotalConsumptions = GetPIDMethods(AppType.Type.All, MethodType.Type.Calltree);
        }
        /// <summary>
        /// Retrieve the PID's methods according to a method type and an app type
        /// </summary>
        /// <param name="appType"></param>
        /// <param name="methodType"></param>
        /// <returns></returns>
        public List<Method> GetPIDMethods(AppType.Type appType, MethodType.Type methodType)
        {
            List<Method> methods = new List<Method>();
            DirectoryInfo pIDFolder = GetPIDFolder();
            if (pIDFolder == null) { return null; }

            string pIDMethodsSubFolderPath;
            if(appType == AppType.Type.App)
            {
                pIDMethodsSubFolderPath = "app/total/";
            }
            else
            {
                pIDMethodsSubFolderPath = "all/total/";
            }
            if(methodType == MethodType.Type.Method)
            {
                pIDMethodsSubFolderPath += "methods";
            }
            else
            {
                pIDMethodsSubFolderPath += "calltrees";
            }

            DirectoryInfo methodsFolder = GetPSubFolderDirectory(pIDFolder.FullName, pIDMethodsSubFolderPath);
            if (methodsFolder == null) { return null; }

            string methodsFileContent = GetCSVFiles(methodsFolder);
            if(methodsFileContent == null) { return null; }

            methods = RegularExpressions.ExtractMethods(methodsFileContent);
            methods = methods.OrderByDescending(m => m.PowerConsumption).ToList();
            return methods;
        }
        /// <summary>
        /// Retrieve the list of a method's consumption per timestamp
        /// </summary>
        /// <param name="method"></param>
        /// <param name="appType"></param>
        /// <returns></returns>
        public static List<MethodConsumptionPerTimestamp> GetMethodConsumptionEvolution(Method method, AppType.Type appType)
        {
            List<MethodConsumptionPerTimestamp> methodConsumptionPerTime = new List<MethodConsumptionPerTimestamp>();
            DirectoryInfo pIDFolder = GetPIDFolder();
            if (pIDFolder == null) { return null; }

            string methodEvolutionSubFolderPath;
            if (appType == AppType.Type.App)
            {
                methodEvolutionSubFolderPath = "app/evolution";
            }
            else
            {
                methodEvolutionSubFolderPath = "all/evolution";
            }

            DirectoryInfo methodConsumptionEvolutionFolder = GetPSubFolderDirectory(pIDFolder.FullName, methodEvolutionSubFolderPath);
            if (methodConsumptionEvolutionFolder == null) { return null; }

            string methodsFilesContent = GetCSVFiles(methodConsumptionEvolutionFolder, method);
            if (methodsFilesContent == null) { return null; }

            methodConsumptionPerTime = RegularExpressions.ExtractMethodConsumptionEvolution(methodsFilesContent);
            methodConsumptionPerTime = methodConsumptionPerTime.OrderBy(m => m.Timestamp).ToList();
            return methodConsumptionPerTime;
        }
        /// <summary>
        /// Retrieve a sub folder directory from a parent folder
        /// </summary>
        /// <param name="parentFolder"></param>
        /// <param name="subFolder"></param>
        /// <returns></returns>
        private static DirectoryInfo GetPSubFolderDirectory(string parentFolder, string subFolder)
        {
            DirectoryInfo subFolderDirectory;
            string subFolderPath = System.IO.Path.Combine(parentFolder, subFolder);
            try
            {
                subFolderDirectory = new DirectoryInfo(subFolderPath);
            }
            catch (Exception ex)
            {
                Dialog.ShowMessage(NavigateToAllMethodPageButton, $"An error occured while retrieving a PID subfolder :\n" +
                                                                    $"{ex.Message}");
                return null;
            }
            return subFolderDirectory;
        }
        /// <summary>
        /// Retrieve the CSV files into a folder
        /// If a method is declared into the parameters, it will retrieve only the CSV whose name contains the method's name
        /// </summary>
        /// <param name="folder"></param>
        /// <param name="method"></param>
        /// <returns></returns>
        private static string GetCSVFiles(DirectoryInfo folder, Method method = null)
        {
            string filesContent = String.Empty;
            FileInfo[] csvFiles;
            try
            {
                //Retrieve the csv files info
                if(method != null)
                {
                    csvFiles = folder.EnumerateFiles($"*{method.Name}*.csv").ToArray();
                }
                else
                {
                    csvFiles = folder.EnumerateFiles("*.csv").ToArray();
                }
                //Retrieve the csv files contents
                foreach (FileInfo csvFile in csvFiles)
                {
                    using (StreamReader reader = new StreamReader(csvFile.FullName))
                    {
                        string line;
                        while ((line = reader.ReadLine()) != null)
                        {
                            if (!String.IsNullOrEmpty(line))
                            {
                                filesContent += $"{line}\n";
                            }
                        }
                    }
                }
            }
            catch (Exception e)
            {
                Dialog.ShowMessage(NavigateToAllMethodPageButton, $"An error occured while the retrieving of the csv files :\n" +
                                                                     $"{e.Message}");
                return null;
            }
            return filesContent;
        }
        /// <summary>
        /// Retrieve the PID directory Info from the selected PID saved into the "CurrentJoularResultsItems" static class
        /// </summary>
        /// <returns></returns>
        private static DirectoryInfo GetPIDFolder()
        {
            DirectoryInfo joularResultsFolder = null;
            DirectoryInfo PIDFolder = null;
            try
            {
                joularResultsFolder = new DirectoryInfo(CurrentJoularResultsItems.joularResultsFolderPath);
                PIDFolder = joularResultsFolder.GetDirectories()
                                .Where(dir => dir.Name == CurrentJoularResultsItems.PID.Name)
                                .FirstOrDefault();
            }
            catch (Exception e)
            {
                Dialog.ShowMessage(NavigateToAllMethodPageButton ,$"Error occured when retrieving joular Results Folder :\n" +
                                    $"{e.Message}");
            }
            return PIDFolder;
        }
    }
}
