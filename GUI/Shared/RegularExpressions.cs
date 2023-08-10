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
using System.Globalization;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading.Tasks;

namespace org_noureddine_joularjx_gui.Shared
{
    /// <summary>
    /// The regex patterns and methods used into the application
    /// </summary>
    public static class RegularExpressions
    {
        public static readonly string PIDNameRegexPattern = @"^(\d*)-(\d*$)";
        public static readonly string MethodsTotalConsumptionsFileContentPattern = @"^(?<Method>[^,]*),(?<Consumption>([\d\w.]*))$";
        public static readonly string MethodsConsumptionsEvolutionFilesContentPattern = @"^(?<Timestamp>\d*),(?<Consumption>[\d.\w]*)$";

        /// <summary>
        /// Permits to check if a text matches with a regex pattern the result as a boolean
        /// </summary>
        /// <param name="text"></param>
        /// <param name="regexPattern"></param>
        /// <returns>the result</returns>
        public static bool IsMatch(string text, string regexPattern)
        {
            bool isMatch = false;

            try
            {
                Regex regex = new Regex(regexPattern);
                isMatch = regex.IsMatch(text);
            }
            catch (ArgumentNullException ex)
            {
                Console.WriteLine("Regex Error : " + ex.Message);
            }
            catch (Exception ex)
            {
                Console.WriteLine("An error Occured : " + ex.Message);
            }

            return isMatch;
        }
        /// <summary>
        /// Extract the content of a file that contains methods total consumptions
        /// </summary>
        /// <param name="text"></param>
        /// <returns></returns>
        public static List<Method> ExtractMethods(string text)
        {
            List<Method> methods = new List<Method>();
            string pattern = MethodsTotalConsumptionsFileContentPattern;
            Regex regex = new Regex(pattern, RegexOptions.Multiline);
            MatchCollection matches = regex.Matches(text);
            foreach (Match match in matches)
            {
                string methodName = match.Groups["Method"].Value;
                string consumptionValue = match.Groups["Consumption"].Value;

                if (!double.TryParse(consumptionValue, NumberStyles.Any, CultureInfo.InvariantCulture, out double consumption))
                {
                    consumption = 0;
                }
                Method method = new Method(methodName, consumption);
                methods.Add(method);
            }
            return methods;
        }
        /// <summary>
        /// Extract the content of a file that contains a method consumption evolution
        /// </summary>
        /// <param name="text"></param>
        /// <returns></returns>
        public static List<MethodConsumptionPerTimestamp> ExtractMethodConsumptionEvolution(string text)
        {
            List<MethodConsumptionPerTimestamp> methodConsumptionPerTime = new List<MethodConsumptionPerTimestamp>();
            string pattern = MethodsConsumptionsEvolutionFilesContentPattern;
            Regex regex = new Regex(pattern, RegexOptions.Multiline);
            MatchCollection matches = regex.Matches(text);
            foreach (Match match in matches)
            {
                string timestampValue = match.Groups["Timestamp"].Value;
                string consumptionValue = match.Groups["Consumption"].Value;
                if (!double.TryParse(consumptionValue, NumberStyles.Any, CultureInfo.InvariantCulture, out double consumption))
                {
                    consumption = 0;
                }
                if(int.TryParse(timestampValue, NumberStyles.Any, CultureInfo.InvariantCulture, out int timestamp))
                {
                    MethodConsumptionPerTimestamp timeConsumption = new MethodConsumptionPerTimestamp(consumption, timestamp);
                    methodConsumptionPerTime.Add(timeConsumption);
                }
            }
            return methodConsumptionPerTime;
        }
    }
}
