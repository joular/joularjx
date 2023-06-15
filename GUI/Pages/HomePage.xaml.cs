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
using System.Collections.Generic;
using System.IO;
using System.Linq;

namespace org_noureddine_joularjx_gui.Pages
{
    /// <summary>
    /// The home page of the application
    /// </summary>
    public sealed partial class HomePage : Page
    {
        private bool isFolderPickerOpen = false;
        public HomePage()
        {
            this.InitializeComponent();
            joularResultsPathTextBlock.Text = CurrentJoularResultsItems.joularResultsFolderPath;
        }
        /// <summary>
        /// Open a folder picker and saves the selected path
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private async void SelectFolderButton_Click(object sender, RoutedEventArgs e)
        {
            if (isFolderPickerOpen) {
                Dialog.ShowMessage(sender as Button, "A folder picker is already open");
                return; 
            }
            isFolderPickerOpen = true;
            MainWindow temporaryMainWindow = new();
            WindowManagement.CenterWindow(temporaryMainWindow);
            await temporaryMainWindow.SaveJoularResultsFolderFromFolderPicker(sender, e);
            joularResultsPathTextBlock.Text = CurrentJoularResultsItems.joularResultsFolderPath;
            isFolderPickerOpen = false;

        }
        /// <summary>
        /// Check if the selected folder is a joular results folder before navigating to "AllPIDsPage"
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void StartAnalysisButton_Click(object sender, RoutedEventArgs e)
        {
            //Check if a folder has been selected
            if (string.IsNullOrEmpty(joularResultsPathTextBlock.Text))
            {
                Dialog.ShowMessage(startAnalysisButton ,"Please select a folder", "Selecting a Joular Results folder is recquired !");
                return;
            }
            DirectoryInfo folderSelected = new(joularResultsPathTextBlock.Text);
            //Check if the selected folder is a joular results folder
            if (!IsFolderSelectedIsAJoularResultsFolder(folderSelected))
            {
                string errorMessage = "The folder selected is not a joular results.\n" +
                                        "Please make sure your folder contains at least one PID";              
                Dialog.ShowMessage(selectFolderButton ,errorMessage);
                return;
            }
            CurrentJoularResultsItems.joularResultsFolderPath = joularResultsPathTextBlock.Text;
            //Retrieve PIDs Folders
            List<DirectoryInfo> pIDsFolders = GetPIDsFolders(folderSelected);
            //Retrieve PIDs Cards
            CurrentJoularResultsItems.PIDs = GetPIDs(pIDsFolders);
            NavigateToAllPIDsPage();
        }
        /// <summary>
        /// Retrieve the PIDs objects trough the PID's folders
        /// </summary>
        /// <param name="pIDsFolders"></param>
        /// <returns></returns>
        private static List<PID> GetPIDs(List<DirectoryInfo> pIDsFolders)
        {
            List<PID> pIDs = new();
            foreach (DirectoryInfo pIDFolder in pIDsFolders)
            {
                PID pID = new()
                {
                    Name = pIDFolder.Name,
                    CreationDate = pIDFolder.LastWriteTime.ToString("yyyy/MM/dd HH:mm:ss")
                };
                pIDs.Add(pID);
            }
            pIDs = pIDs.OrderByDescending(p => p.CreationDate).ToList();
            return pIDs;
        }
        /// <summary>
        /// Retrieve PIDs folders from a joular results folder
        /// </summary>
        /// <param name="joularResultsFolder"></param>
        /// <returns></returns>
        private static List<DirectoryInfo> GetPIDsFolders(DirectoryInfo joularResultsFolder)
        {
            List<DirectoryInfo> pIDsFolders = new();

            foreach(DirectoryInfo directory in joularResultsFolder.GetDirectories())
            {
                if(RegularExpressions.IsMatch(directory.Name, RegularExpressions.PIDNameRegexPattern))
                {
                    pIDsFolders.Add(directory);
                }
            }

            return pIDsFolders;
        }
        /// <summary>
        /// Check if a folder is a joular results folder
        /// </summary>
        /// <param name="folderSelected"></param>
        /// <returns></returns>
        private static bool IsFolderSelectedIsAJoularResultsFolder(DirectoryInfo folderSelected)
        {
            bool folderSelectedIsJoularResultsFolder = false;
            foreach (DirectoryInfo folder in folderSelected.GetDirectories())
            {
                if (RegularExpressions.IsMatch(folder.Name, RegularExpressions.PIDNameRegexPattern))
                {
                    folderSelectedIsJoularResultsFolder = true;
                    break;
                }
            }
            return folderSelectedIsJoularResultsFolder;
        }
        /// <summary>
        /// Open the "AllPIDsPage" page into the current frame
        /// </summary>
        private void NavigateToAllPIDsPage()
        {
            Frame frame = this.FindFirstParentOfType<Frame>();
            NavigationView navigationView = this.FindFirstParentOfType<NavigationView>();
            //Enable the "All PIDs" navigation item
            foreach (NavigationViewItemBase item in navigationView.MenuItems.Cast<NavigationViewItemBase>())
            {
                if ((item is NavigationViewItem navigationItem) && (navigationItem.Tag?.ToString() == "AllPIDsPage"))
                {
                    navigationItem.IsEnabled = true;
                }
            }
            //Navigate to "AllPIDsPage" page
            navigationView.SelectedItem = navigationView.MenuItems.OfType<NavigationViewItem>()
                                                        .FirstOrDefault(item => item.Tag.ToString() == "AllPIDsPage");
        }
    }
}
