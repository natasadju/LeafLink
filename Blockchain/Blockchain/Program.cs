using MPI;
using System;
using System.Windows.Forms;

namespace Blockchain
{
    internal static class Program
    {
        [STAThread]
        static void Main(string[] args)
        {
            MPI.Environment.Run(ref args, comm =>
            {
                if (comm.Rank == 0) // Master Node: Run GUI
                {
                    Application.EnableVisualStyles();
                    Application.SetCompatibleTextRenderingDefault(false);
                    Application.Run(new Form1());
                }
                else // Worker Nodes: Run mining tasks
                {
                    Console.WriteLine($"Worker Node {comm.Rank} started.");
                    MPIManager.WorkerNode(comm);
                }
            });
        }
    }
}
